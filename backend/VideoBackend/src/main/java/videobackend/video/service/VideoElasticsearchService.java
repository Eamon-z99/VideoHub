package videobackend.video.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import videobackend.video.model.VideoItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频全文搜索：索引字段 title / description / uploaderName，中文使用 ES 内置 cjk 分析器。
 * 需配置 {@code videohub.elasticsearch.enabled=true} 与 {@code spring.elasticsearch.uris}，且存在 {@link ElasticsearchClient} Bean。
 */
@Service
@ConditionalOnProperty(prefix = "videohub.elasticsearch", name = "enabled", havingValue = "true")
@ConditionalOnBean(ElasticsearchClient.class)
public class VideoElasticsearchService {

    private static final Logger log = LoggerFactory.getLogger(VideoElasticsearchService.class);

    private final ElasticsearchClient client;
    private final JdbcTemplate jdbcTemplate;
    private final LocalVideoService localVideoService;
    private final String indexName;

    public VideoElasticsearchService(ElasticsearchClient client,
                                     JdbcTemplate jdbcTemplate,
                                     LocalVideoService localVideoService,
                                     @org.springframework.beans.factory.annotation.Value("${videohub.elasticsearch.index-name:videohub_videos}") String indexName) {
        this.client = client;
        this.jdbcTemplate = jdbcTemplate;
        this.localVideoService = localVideoService;
        this.indexName = indexName;
    }

    public void ensureIndexExists() throws Exception {
        boolean exists = client.indices().exists(e -> e.index(indexName)).value();
        if (exists) {
            return;
        }
        client.indices().create(c -> c
                .index(indexName)
                .mappings(m -> m
                        .properties("videoId", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t.analyzer("cjk")))
                        .properties("description", p -> p.text(t -> t.analyzer("cjk")))
                        .properties("uploaderName", p -> p.text(t -> t.analyzer("cjk")))
                ));
        log.info("Created Elasticsearch index {}", indexName);
    }

    /**
     * 从 MySQL 全量重建索引（会先删除已存在的索引再创建）。
     */
    public synchronized void fullReindex() {
        try {
            try {
                client.indices().delete(d -> d.index(indexName));
                log.info("Dropped Elasticsearch index {}", indexName);
            } catch (Exception e) {
                log.debug("Index drop skipped: {}", e.getMessage());
            }
            ensureIndexExists();

            String sql = """
                    SELECT v.video_id,
                           COALESCE(v.title, '') AS title,
                           COALESCE(v.description, '') AS description,
                           COALESCE(u.username, '') AS uploader_name
                    FROM videos v
                    LEFT JOIN users u ON v.user_id = u.id
                    WHERE (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                    """;

            List<Map<String, Object>> batch = new ArrayList<>();
            jdbcTemplate.query(sql, rs -> {
                try {
                    Map<String, Object> doc = new LinkedHashMap<>();
                    doc.put("videoId", rs.getString("video_id"));
                    doc.put("title", rs.getString("title"));
                    doc.put("description", rs.getString("description"));
                    doc.put("uploaderName", rs.getString("uploader_name"));
                    batch.add(doc);
                    if (batch.size() >= 400) {
                        flushBulk(batch);
                        batch.clear();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            if (!batch.isEmpty()) {
                flushBulk(batch);
            }
            client.indices().refresh(r -> r.index(indexName));
            log.info("Elasticsearch full reindex finished for index {}", indexName);
        } catch (Exception e) {
            log.error("Elasticsearch full reindex failed", e);
            throw new RuntimeException("Elasticsearch reindex failed: " + e.getMessage(), e);
        }
    }

    private void flushBulk(List<Map<String, Object>> docs) throws Exception {
        if (docs.isEmpty()) {
            return;
        }
        List<BulkOperation> ops = new ArrayList<>();
        for (Map<String, Object> doc : docs) {
            String id = (String) doc.get("videoId");
            if (!StringUtils.hasText(id)) {
                continue;
            }
            ops.add(BulkOperation.of(b -> b.index(i -> i
                    .index(indexName)
                    .id(id)
                    .document(doc))));
        }
        if (ops.isEmpty()) {
            return;
        }
        var resp = client.bulk(b -> b.operations(ops));
        if (resp.errors()) {
            log.warn("Bulk index had errors: {}", resp.items().stream()
                    .filter(i -> i.error() != null)
                    .findFirst()
                    .map(i -> i.error().reason())
                    .orElse("unknown"));
        }
    }

    public Map<String, Object> search(String keyword, int page, int pageSize) throws Exception {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        if (!StringUtils.hasText(keyword)) {
            return Map.of(
                    "list", List.<VideoItem>of(),
                    "page", safePage,
                    "pageSize", safeSize,
                    "total", 0L
            );
        }

        ensureIndexExists();

        String q = keyword.trim();
        @SuppressWarnings("rawtypes")
        SearchResponse<Map> response = client.search(s -> s
                        .index(indexName)
                        .query(query -> query.multiMatch(m -> m
                                .query(q)
                                .fields("title^3", "description^2", "uploaderName^2")
                                .type(TextQueryType.BestFields)
                                .fuzziness("AUTO")))
                        .from((safePage - 1) * safeSize)
                        .size(safeSize)
                        .trackTotalHits(t -> t.enabled(true)),
                Map.class);

        long total = response.hits().total() == null ? 0L : response.hits().total().value();
        List<String> ids = new ArrayList<>();
        for (var hit : response.hits().hits()) {
            Map<String, Object> src = hit.source();
            if (src == null) {
                continue;
            }
            Object vid = src.get("videoId");
            if (vid != null && StringUtils.hasText(vid.toString())) {
                ids.add(vid.toString());
            }
        }

        List<VideoItem> items = localVideoService.listByVideoIdsPreserveOrder(ids);
        return Map.of(
                "list", items,
                "page", safePage,
                "pageSize", safeSize,
                "total", total
        );
    }
}
