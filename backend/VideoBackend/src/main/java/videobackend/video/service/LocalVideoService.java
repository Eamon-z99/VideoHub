package videobackend.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import videobackend.video.model.VideoItem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocalVideoService {

    private static final Logger log = LoggerFactory.getLogger(LocalVideoService.class);
    private final JdbcTemplate jdbcTemplate;
    private final Path mediaRoot;
    private final String cdnBaseUrl;
    private final boolean useCdn;
    private final MediaUrlResolver mediaUrlResolver;

    public LocalVideoService(JdbcTemplate jdbcTemplate,
                             @Value("${media.storage.root}") String mediaStorageRoot,
                             @Value("${media.cdn.base-url:}") String cdnBaseUrl,
                             @Value("${media.cdn.enabled:false}") boolean useCdn,
                             MediaUrlResolver mediaUrlResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.mediaRoot = Paths.get(mediaStorageRoot);
        // 统一去掉末尾 /，便于后续拼接
        this.cdnBaseUrl = StringUtils.hasText(cdnBaseUrl)
                ? cdnBaseUrl.replaceAll("/+$", "")
                : "";
        this.useCdn = useCdn;
        if (this.useCdn && !StringUtils.hasText(this.cdnBaseUrl)) {
            throw new IllegalStateException("media.cdn.enabled=true 但未配置 media.cdn.base-url");
        }
        log.info("Media CDN enabled: {}, baseUrl: {}", this.useCdn, this.cdnBaseUrl);
        this.mediaUrlResolver = mediaUrlResolver;
    }

    private static final DateTimeFormatter UPLOAD_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM-dd");

    public List<VideoItem> listPage(int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        // 使用随机顺序返回视频列表（MySQL: ORDER BY RAND()），避免前端再做乱序
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN')
                ORDER BY RAND()
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), safeSize, offset);
    }

    /**
     * 按播放量倒序获取前 N 个视频（用于首页推荐/轮播）
     */
    public List<VideoItem> listTopByViewCount(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN')
                ORDER BY v.view_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), safeLimit);
    }

    public long count() {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM videos WHERE status IS NULL OR UPPER(TRIM(status)) NOT IN ('FAILED','DOWN')", Long.class);
        return total == null ? 0 : total;
    }

    /**
     * tags 字段为 JSON 数组字符串，筛选包含指定标签名的视频（精确匹配数组中的某个字符串元素）。
     */
    public List<VideoItem> listPageByTag(String tag, int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        String t = tag == null ? "" : tag.trim();
        if (!StringUtils.hasText(t)) {
            return listPage(page, pageSize);
        }
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                  AND v.tags IS NOT NULL
                  AND TRIM(v.tags) <> ''
                  AND JSON_VALID(v.tags)
                  AND JSON_SEARCH(v.tags, 'one', ?, NULL, '$[*]') IS NOT NULL
                ORDER BY RAND()
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), t, safeSize, offset);
    }

    public long countByTag(String tag) {
        String t = tag == null ? "" : tag.trim();
        if (!StringUtils.hasText(t)) {
            return count();
        }
        String sql = """
                SELECT COUNT(*)
                FROM videos v
                WHERE (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                  AND v.tags IS NOT NULL
                  AND TRIM(v.tags) <> ''
                  AND JSON_VALID(v.tags)
                  AND JSON_SEARCH(v.tags, 'one', ?, NULL, '$[*]') IS NOT NULL
                """;
        Long total = jdbcTemplate.queryForObject(sql, Long.class, t);
        return total == null ? 0 : total;
    }

    /**
     * partition 字段为 JSON 数组字符串时，筛选包含指定快捷入口标签的视频（与 listPageByTag 逻辑相同，列不同）。
     */
    public List<VideoItem> listPageByPartitionLabel(String label, int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        String t = label == null ? "" : label.trim();
        if (!StringUtils.hasText(t)) {
            return listPage(page, pageSize);
        }
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                  AND v.`partition` IS NOT NULL
                  AND TRIM(v.`partition`) <> ''
                  AND JSON_VALID(v.`partition`)
                  AND JSON_SEARCH(v.`partition`, 'one', ?, NULL, '$[*]') IS NOT NULL
                ORDER BY RAND()
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), t, safeSize, offset);
    }

    public long countByPartitionLabel(String label) {
        String t = label == null ? "" : label.trim();
        if (!StringUtils.hasText(t)) {
            return count();
        }
        String sql = """
                SELECT COUNT(*)
                FROM videos v
                WHERE (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                  AND v.`partition` IS NOT NULL
                  AND TRIM(v.`partition`) <> ''
                  AND JSON_VALID(v.`partition`)
                  AND JSON_SEARCH(v.`partition`, 'one', ?, NULL, '$[*]') IS NOT NULL
                """;
        Long total = jdbcTemplate.queryForObject(sql, Long.class, t);
        return total == null ? 0 : total;
    }

    /**
     * 按关键字搜索视频（标题 / 描述 / 作者昵称）
     */
    public List<VideoItem> listPageByKeyword(String keyword, int page, int pageSize) {
        if (keyword == null || keyword.isBlank()) {
            return listPage(page, pageSize);
        }
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        String like = "%" + keyword.trim() + "%";

        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE (v.title LIKE ?
                   OR v.description LIKE ?
                   OR u.username LIKE ?)
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                ORDER BY v.create_time DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs),
                like, like, like, safeSize, offset);
    }

    /**
     * 管理端视频列表：用于「勾选首页 hero-grid 轮播内容」。
     * - keyword 为空：按创建时间倒序（避免 listPage 的 ORDER BY RAND() 随机翻页）
     * - keyword 非空：复用 listPageByKeyword（按 create_time desc）
     */
    public List<VideoItem> listAdminPage(int page, int pageSize, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return listPageByKeyword(keyword, page, pageSize);
        }

        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                ORDER BY v.create_time DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), safeSize, offset);
    }

    public long countAdminByKeyword(String keyword) {
        return countByKeyword(keyword);
    }

    public long countByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return count();
        }
        String like = "%" + keyword.trim() + "%";
        String sql = """
                SELECT COUNT(*)
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE (v.title LIKE ?
                   OR v.description LIKE ?
                   OR u.username LIKE ?)
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                """;
        Long total = jdbcTemplate.queryForObject(sql, Long.class, like, like, like);
        return total == null ? 0 : total;
    }

    /**
     * 按给定 video_id 顺序返回完整 {@link VideoItem}（用于 ES 检索后回表）。
     */
    public List<VideoItem> listByVideoIdsPreserveOrder(List<String> orderedVideoIds) {
        if (orderedVideoIds == null || orderedVideoIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String id : orderedVideoIds) {
            if (StringUtils.hasText(id)) {
                unique.add(id.trim());
            }
        }
        List<String> ids = new ArrayList<>(unique);
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.video_id IN (%s)
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                ORDER BY FIELD(v.video_id, %s)
                """.formatted(placeholders, placeholders);
        List<Object> args = new ArrayList<>();
        args.addAll(ids);
        args.addAll(ids);
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), args.toArray());
    }

    /**
     * 获取关注用户的视频列表（分页）
     */
    public List<VideoItem> listPageByFollowing(Long userId, Long followingId, int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                INNER JOIN fans f ON v.user_id = f.following_id
                LEFT JOIN users u ON v.user_id = u.id
                WHERE f.follower_id = ?
                  AND (? IS NULL OR f.following_id = ?)
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                ORDER BY v.create_time DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), userId, followingId, followingId, safeSize, offset);
    }

    /**
     * 获取关注用户的视频总数
     */
    public long countByFollowing(Long userId, Long followingId) {
        String sql = """
                SELECT COUNT(*)
                FROM videos v
                INNER JOIN fans f ON v.user_id = f.following_id
                WHERE f.follower_id = ?
                  AND (? IS NULL OR f.following_id = ?)
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                """;
        Long total = jdbcTemplate.queryForObject(sql, Long.class, userId, followingId, followingId);
        return total == null ? 0 : total;
    }

    public Optional<VideoItem> findByVideoId(String videoId) {
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.`partition`,
                       v.`tags`,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.video_id = ?
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                """;
        List<VideoItem> list = jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), videoId);
        return list.stream().findFirst();
    }

    public Optional<VideoItem> findByVideoIdIncludingTakedown(String videoId) {
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.`partition`,
                       v.`tags`,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.video_id = ?
                """;
        List<VideoItem> list = jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), videoId);
        return list.stream().findFirst();
    }

    public record VideoAccessInfo(Long ownerUserId, String status) {}

    public Optional<VideoAccessInfo> getVideoAccessInfo(String videoId) {
        String sql = """
                SELECT user_id, status
                FROM videos
                WHERE video_id = ?
                LIMIT 1
                """;
        List<VideoAccessInfo> list = jdbcTemplate.query(sql, (rs, i) ->
                new VideoAccessInfo(
                        rs.getObject("user_id") == null ? null : rs.getLong("user_id"),
                        rs.getString("status")
                ), videoId);
        return list.stream().findFirst();
    }

    /**
     * 获取指定作者的其它视频（按时间倒序），可排除当前视频
     */
    public List<VideoItem> listByUploader(Long uploaderId, String excludeVideoId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.user_id = ?
                  AND (? IS NULL OR v.video_id <> ?)
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                ORDER BY v.create_time DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs),
                uploaderId, excludeVideoId, excludeVideoId, safeLimit);
    }

    /**
     * 指定 UP 主已发布视频分页（投稿 Tab），按创建时间倒序。
     *
     * @param collectionFilter null 表示全部；0 表示仅未加入合集（collection_id IS NULL）；正数为某合集 ID
     */
    public List<VideoItem> listPageByUploader(Long uploaderId, int page, int pageSize, Long collectionFilter) {
        if (uploaderId == null) {
            return List.of();
        }
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        String coll = uploaderCollectionPredicate(collectionFilter);
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.user_id = ?
                """ + coll + """
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                ORDER BY v.create_time DESC
                LIMIT ? OFFSET ?
                """;
        if (collectionFilter != null && collectionFilter != 0L) {
            return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), uploaderId, collectionFilter, safeSize, offset);
        }
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), uploaderId, safeSize, offset);
    }

    /**
     * 指定 UP 的投稿列表 + 关键字（标题 / 描述）
     */
    public List<VideoItem> listPageByUploaderKeyword(Long uploaderId, String keyword, int page, int pageSize, Long collectionFilter) {
        if (uploaderId == null) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return listPageByUploader(uploaderId, page, pageSize, collectionFilter);
        }
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        String like = "%" + keyword.trim() + "%";
        String coll = uploaderCollectionPredicate(collectionFilter);
        String sql = """
                SELECT v.video_id,
                       v.title,
                       v.description,
                       v.duration,
                       v.cover_url,
                       v.storage_path,
                       v.source_file,
                       v.view_count,
                       (SELECT COUNT(*)
                        FROM comments c
                        WHERE c.video_id = v.video_id
                          AND c.status = 1
                       ) AS comment_count,
                       v.like_count,
                       v.favorite_count,
                       v.file_size,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id,
                       v.create_time
                FROM videos v
                LEFT JOIN users u ON v.user_id = u.id
                WHERE v.user_id = ?
                  AND (v.title LIKE ? OR COALESCE(v.description, '') LIKE ?)
                """ + coll + """
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                ORDER BY v.create_time DESC
                LIMIT ? OFFSET ?
                """;
        if (collectionFilter != null && collectionFilter != 0L) {
            return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), uploaderId, like, like, collectionFilter, safeSize, offset);
        }
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), uploaderId, like, like, safeSize, offset);
    }

    public long countByUploaderKeyword(Long uploaderId, String keyword, Long collectionFilter) {
        if (uploaderId == null) {
            return 0;
        }
        if (keyword == null || keyword.isBlank()) {
            return countByUploader(uploaderId, collectionFilter);
        }
        String like = "%" + keyword.trim() + "%";
        String coll = uploaderCollectionPredicate(collectionFilter);
        String sql = """
                SELECT COUNT(*)
                FROM videos v
                WHERE v.user_id = ?
                  AND (v.title LIKE ? OR COALESCE(v.description, '') LIKE ?)
                """ + coll + """
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                """;
        if (collectionFilter != null && collectionFilter != 0L) {
            Long total = jdbcTemplate.queryForObject(sql, Long.class, uploaderId, like, like, collectionFilter);
            return total == null ? 0 : total;
        }
        Long total = jdbcTemplate.queryForObject(sql, Long.class, uploaderId, like, like);
        return total == null ? 0 : total;
    }

    public long countByUploader(Long uploaderId, Long collectionFilter) {
        if (uploaderId == null) {
            return 0;
        }
        String coll = uploaderCollectionPredicate(collectionFilter);
        String sql = """
                SELECT COUNT(*)
                FROM videos v
                WHERE v.user_id = ?
                """ + coll + """
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                """;
        if (collectionFilter != null && collectionFilter != 0L) {
            Long total = jdbcTemplate.queryForObject(sql, Long.class, uploaderId, collectionFilter);
            return total == null ? 0 : total;
        }
        Long total = jdbcTemplate.queryForObject(sql, Long.class, uploaderId);
        return total == null ? 0 : total;
    }

    /**
     * 投稿合集筛选：null=不筛选；0=未加入合集；正数=指定合集。
     */
    private static String uploaderCollectionPredicate(Long collectionFilter) {
        if (collectionFilter == null) {
            return "";
        }
        if (collectionFilter == 0L) {
            return " AND v.collection_id IS NULL ";
        }
        return " AND v.collection_id = ? ";
    }

    private VideoItem mapToVideo(ResultSet rs) throws SQLException {
        String sourceFile = rs.getString("source_file");
        String storagePath = rs.getString("storage_path");
        String coverFile = rs.getString("cover_url");

        // 优先使用数据库中的用户名，其次用目录名推导
        String uploaderFromDb = rs.getString("uploader_name");
        String uploaderName = firstNonBlank(uploaderFromDb, buildUploaderName(sourceFile));
        
        // 获取作者头像和ID
        String uploaderAvatar = null;
        Long uploaderId = null;
        try {
            uploaderAvatar = rs.getString("uploader_avatar");
            Object uploaderIdObj = rs.getObject("uploader_id");
            if (uploaderIdObj instanceof Number) {
                uploaderId = ((Number) uploaderIdObj).longValue();
            }
        } catch (SQLException e) {
            // 如果字段不存在（旧查询），忽略
        }

        // 上传日期：优先用视频表 create_time，格式 MM-dd
        String uploadDate = null;
        String createTimeStr = null;
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            LocalDate date = createTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            uploadDate = UPLOAD_DATE_FORMATTER.format(date);
            // 转换为 ISO 8601 格式字符串（包含时区）
            createTimeStr = createTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        // comments 统计：与 CommentService.countCommentsWithReplies 一致（含回复、status=1）；仅在部分查询返回 comment_count 时生效
        Long commentCount = 0L;
        try {
            Object ccObj = rs.getObject("comment_count");
            if (ccObj != null) {
                commentCount = ((Number) ccObj).longValue();
            }
        } catch (SQLException ignore) {
            // 旧查询未选择 comment_count 字段时兼容
        }

        String tagsJson = null;
        try {
            tagsJson = rs.getString("tags");
        } catch (SQLException ignore) {
            // 旧查询未选择 tags 字段
        }

        String partition = null;
        try {
            partition = rs.getString("partition");
        } catch (SQLException ignore) {
            // 旧查询未选择 partition 字段
        }

        return new VideoItem(
                rs.getString("video_id"),
                firstNonBlank(rs.getString("title"), "本地视频"),
                rs.getString("description"),
                rs.getObject("duration") == null ? null : rs.getInt("duration"),
                buildLocalUrl(sourceFile, coverFile),
                buildLocalUrl(sourceFile, storagePath),
                storagePath,
                sourceFile,
                rs.getObject("view_count") == null ? null : rs.getLong("view_count"),
                commentCount,
                rs.getObject("like_count") == null ? null : rs.getLong("like_count"),
                rs.getObject("favorite_count") == null ? null : rs.getLong("favorite_count"),
                rs.getObject("file_size") == null ? null : rs.getLong("file_size"),
                uploaderName,
                uploadDate,
                mediaUrlResolver.resolveAvatar(uploaderAvatar),
                uploaderId,
                tagsJson,
                partition,
                createTimeStr
        );
    }

    /**
     * 从 source_file 推导一个“UP”显示名，例如 "Videos/290/xxx.mp4" -> "Videos/290"
     */
    private String buildUploaderName(String sourceFile) {
        if (!StringUtils.hasText(sourceFile)) {
            return "本地媒体库";
        }
        String normalized = sourceFile.replace("\\", "/");
        // 去掉开头的 /
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        String[] parts = normalized.split("/");
        if (parts.length >= 2) {
            // 使用前两个层级，形如 "Videos/290"
            return parts[0] + "/" + parts[1];
        }
        // 只有一层目录时直接返回
        return parts[0];
    }

    private String buildLocalUrl(String sourceFile, String fileName) {
        if (!StringUtils.hasText(sourceFile) || !StringUtils.hasText(fileName)) {
            return "";
        }

        // 启用 CDN 时，直接返回 CDN 地址（无需检查本地文件存在）
        if (useCdn && StringUtils.hasText(cdnBaseUrl)) {
            String normalizedSource = normalizeSource(safeDecode(sourceFile)).replace("\\", "/");
            String normalizedFile = safeDecode(fileName).replace("\\", "/");

            // 组合相对路径（如果 fileName 已包含路径则直接用）
            String relativePath;
            if (normalizedFile.contains("/")) {
                relativePath = normalizedFile;
            } else {
                String base = normalizedSource.startsWith("/") ? normalizedSource.substring(1) : normalizedSource;
                relativePath = StringUtils.hasText(base) ? base + "/" + normalizedFile : normalizedFile;
            }

            String encoded = Arrays.stream(relativePath.split("/"))
                    .filter(StringUtils::hasText)
                    .map(seg -> UriUtils.encodePathSegment(seg, StandardCharsets.UTF_8))
                    .collect(Collectors.joining("/"));

            return cdnBaseUrl + "/" + encoded;
        }

        // 兼容数据库中已存的 URL 编码路径或包含 % 的原始文件名
        String normalizedSource = normalizeSource(safeDecode(sourceFile));
        String normalizedFile = safeDecode(fileName).replace("\\", "/");

        // 判断 fileName 是否已经包含路径（包含斜杠）
        Path finalPath;
        if (normalizedFile.contains("/")) {
            // fileName 已经包含路径，直接使用
            finalPath = mediaRoot.resolve(normalizedFile).normalize();
        } else {
            // fileName 只是文件名，需要拼接 sourceFile 的目录部分
            Path sourcePath = Paths.get(normalizedSource);
            Path baseDir = sourcePath.getFileName() != null && hasFileExtension(sourcePath.getFileName().toString())
                    ? sourcePath.getParent()
                    : sourcePath;
            finalPath = mediaRoot.resolve(baseDir == null ? Paths.get("") : baseDir).resolve(normalizedFile).normalize();
        }

        // 安全检查：确保路径在 mediaRoot 内
        if (!finalPath.startsWith(mediaRoot)) {
            return "";
        }
        
        // 确定目录，同步使用目录实际文件名以避免编码差异
        Path dir = finalPath.getParent();
        boolean needImage = isImage(normalizedFile);

        // 先尝试按文件名在目录中直接匹配（处理 %, 全角/半角、规范化差异）
        Path foundByName = findFileByName(dir, normalizedFile, needImage);
        if (foundByName != null) {
            finalPath = foundByName;
        }

        // 优先尝试用数据库文件名直接匹配文件系统中的文件
        if (!java.nio.file.Files.exists(finalPath)) {
            // 在目录中按文件名精确搜索（处理文件名编码差异）
            Path found = findFileByName(dir, normalizedFile, needImage);
            if (found != null) {
                finalPath = found;
            } else {
                // 尝试其它常见扩展名（图片或视频）
                String baseName = stripExtension(normalizedFile);
                String[] imageExts = {".png", ".jpg", ".jpeg", ".PNG", ".JPG", ".JPEG"};
                String[] videoExts = {".mp4", ".MP4", ".mkv", ".MKV", ".mov", ".MOV"};
                String[] extList = needImage ? imageExts : videoExts;
                for (String ext : extList) {
                    Path altPath = dir.resolve(baseName + ext).normalize();
                    if (altPath.startsWith(mediaRoot) && java.nio.file.Files.exists(altPath)) {
                        finalPath = altPath;
                        break;
                    }
                }
            }
        }

        // 如果仍不存在，使用相似度匹配或取第一条同类型文件
        if (!java.nio.file.Files.exists(finalPath)) {
            Path byName = tryMatchByName(dir, stripExtension(normalizedFile), needImage);
            if (byName != null) {
                finalPath = byName;
            } else {
                Path first = pickFirstByType(dir, needImage);
                if (first != null) {
                    finalPath = first;
                } else {
                    return "";
                }
            }
        }

        // 构建相对路径用于URL编码
        Path relativePath = mediaRoot.relativize(finalPath);
        String encoded = Arrays.stream(relativePath.toString().replace("\\", "/").split("/"))
                .filter(StringUtils::hasText)
                .map(seg -> UriUtils.encodePathSegment(seg, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));

        // CDN 未启用时只走本地映射
        return "/local-videos/" + encoded;
    }

    /**
     * 与视频列表/详情一致：根据 source_file + 封面路径/文件名生成可访问的 /local-videos/... URL。
     * 创作中心内容管理「已发布」列表等场景使用。
     */
    public String buildCoverPublicUrl(String sourceFile, String coverFile) {
        return buildLocalUrl(sourceFile, coverFile);
    }

    /**
     * 尝试对字符串进行 URL 解码，失败则返回原值，避免包含 % 的普通文件名抛异常
     */
    private String safeDecode(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        try {
            return UriUtils.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignore) {
            return value;
        }
    }

    private String stripExtension(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return name;
    }

    private Path findFileByName(Path dir, String targetFileName, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String fileName = p.getFileName().toString();
                // 检查文件类型
                if (imageOnly && !isImage(fileName)) continue;
                if (!imageOnly && !isVideo(fileName)) continue;
                
                // 优先精确匹配（包括包含%的文件名）
                if (fileName.equals(targetFileName)) {
                    return p.normalize();
                }
                
                // 忽略大小写匹配
                if (fileName.equalsIgnoreCase(targetFileName)) {
                    return p.normalize();
                }
                
                // Unicode规范化匹配（但保留%字符）
                String normalizedTarget = normalizeFileNameWithPercent(targetFileName);
                String normalizedFile = normalizeFileNameWithPercent(fileName);
                if (normalizedTarget.equals(normalizedFile)) {
                    return p.normalize();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    
    /**
     * 规范化文件名用于匹配，但保留%字符（不删除）
     */
    private String normalizeFileNameWithPercent(String fileName) {
        // Unicode规范化，统一全角/半角、组合字符等
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        // 统一全角/半角标点（但保留%）
        normalized = normalized.replace('！', '!')
                .replace('？', '?')
                .replace('％', '%')  // 全角%转为半角%
                .replace('—', '-')
                .replace('–', '-')
                .replace('－', '-');
        // 注意：这里不删除%，保留%用于匹配
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeFileName(String fileName) {
        // Unicode规范化，统一全角/半角、组合字符等
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        // 统一全角/半角标点
        normalized = normalized.replace('！', '!')
                .replace('？', '?')
                .replace('％', '%')
                .replace('—', '-')
                .replace('–', '-')
                .replace('－', '-');
        // 移除 % 进行模糊匹配，避免 % 参与比对失败
        normalized = normalized.replace("%", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private boolean hasMultipleSpecialChars(String fileName) {
        // 检测文件名中是否包含多个特殊字符（如"？！"、"！"等全角标点）
        // 这些字符在URL编码后可能导致Spring资源处理器无法正确匹配文件
        int count = 0;
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            // 检测全角标点符号：！(U+FF01)、？(U+FF1F)、—(U+2014)等
            if (c == '！' || c == '？' || c == '—' || c == '–' || c == '－') {
                count++;
                // 如果出现2个或以上特殊字符，直接使用目录扫描
                if (count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private Path tryMatchByName(Path dir, String targetBase, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        String targetNorm = normalizeForCompare(targetBase).replace("%", "");
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String fileName = p.getFileName().toString();
                if (imageOnly && !isImage(fileName)) continue;
                if (!imageOnly && !isVideo(fileName)) continue;

                String candidateBase = stripExtension(fileName);
                String candidateNorm = normalizeForCompare(candidateBase).replace("%", "");
                if (candidateNorm.equals(targetNorm) || candidateNorm.contains(targetNorm) || targetNorm.contains(candidateNorm)) {
                    return p.normalize();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Path pickSingleByType(Path dir, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            Path candidate = null;
            int count = 0;
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (imageOnly && !isImage(name)) continue;
                if (!imageOnly && !isVideo(name)) continue;
                candidate = p.normalize();
                count++;
                if (count > 1) break;
            }
            return count == 1 ? candidate : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Path pickFirstByType(Path dir, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (imageOnly && !isImage(name)) continue;
                if (!imageOnly && !isVideo(name)) continue;
                return p.normalize();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isImage(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private boolean isVideo(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".mov");
    }

    private String normalizeForCompare(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFKC);
        normalized = normalized.replace('—', '-')
                .replace('–', '-')
                .replace('－', '-')
                .replace("%", "")
                .replace(" ", "")
                .replace("　", "");
        // 去除常见标点，仅保留字母数字和中日韩统一表意文字
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (Character.isLetterOrDigit(c) || isCjk(c) || c == '-') {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    private boolean isCjk(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS;
    }

    private boolean hasFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 && dot < name.length() - 1;
    }

    private String normalizeSource(String sourceFile) {
        String normalized = sourceFile.replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!StringUtils.hasText(normalized)) {
            return normalized;
        }
        Path rootFileName = mediaRoot.getFileName();
        if (rootFileName != null) {
            String rootName = rootFileName.toString().toLowerCase(Locale.ROOT);
            if (normalized.toLowerCase(Locale.ROOT).startsWith(rootName + "/")) {
                return normalized.substring(rootName.length() + 1);
            }
        }
        return normalized;
    }

    private String firstNonBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}




