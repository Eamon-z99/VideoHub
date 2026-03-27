package videobackend.video.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchHotKeywordService {

    private static final Logger log = LoggerFactory.getLogger(SearchHotKeywordService.class);

    private final JdbcTemplate jdbcTemplate;

    public SearchHotKeywordService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 记录搜索事件，并确保 search_hot_keywords 中有该 keyword 行
     *
     * 如果数据库表还未创建：这里会抛异常，开发时需要先把 DDL 跑起来
     */
    public void recordKeywordEvent(Long userId, String keyword, int weight) {
        // 1) 写入事件表
        jdbcTemplate.update(
                "INSERT INTO search_keyword_events (user_id, keyword, weight, create_time) VALUES (?, ?, ?, NOW())",
                userId,
                keyword,
                Math.max(1, weight)
        );

        // 2) upsert 到热搜表（用于 isNew/first_seen_time）
        jdbcTemplate.update(
                """
                INSERT INTO search_hot_keywords (keyword, first_seen_time, last_seen_time, score)
                VALUES (?, NOW(), NOW(), 0)
                ON DUPLICATE KEY UPDATE
                  last_seen_time = VALUES(last_seen_time)
                """,
                keyword
        );
    }

    /**
     * 获取热搜列表
     * 返回：[{ keyword, isNew }]
     */
    public List<Map<String, Object>> listHotKeywords(int limit) {
        // score 由 scheduler 计算/刷新；如果 scheduler 未跑过，仍可按 score 查询（默认 0）
        String sql = """
                SELECT keyword, score, first_seen_time
                FROM search_hot_keywords
                ORDER BY score DESC, last_seen_time DESC
                LIMIT ?
                """;

        // 简化：isNew = first_seen_time >= NOW() - 24h
        long nowEpochMs = System.currentTimeMillis();

        List<Map<String, Object>> result = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            String kw = rs.getString("keyword");
            double score = rs.getDouble("score");
            Timestamp firstSeen = rs.getTimestamp("first_seen_time");
            boolean isNew = false;
            if (firstSeen != null) {
                long firstSeenMs = firstSeen.getTime();
                isNew = firstSeenMs >= (nowEpochMs - 24L * 60 * 60 * 1000);
            }
            result.add(Map.of(
                    "keyword", kw,
                    "score", score,
                    "isNew", isNew
            ));
        }, limit);
        return result;
    }
}

