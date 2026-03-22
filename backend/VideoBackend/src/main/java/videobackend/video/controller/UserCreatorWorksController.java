package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.LocalVideoService;
import videobackend.video.util.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创作中心 - 内容管理：合并「已发布视频 + 草稿 + 审核流中的投稿」
 */
@RestController
@RequestMapping("/api/db/creator-works")
public class UserCreatorWorksController {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;
    private final LocalVideoService localVideoService;

    public UserCreatorWorksController(JdbcTemplate jdbcTemplate, JwtUtil jwtUtil, LocalVideoService localVideoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
        this.localVideoService = localVideoService;
    }

    /**
     * @param tab all | draft | reviewing | approved | rejected
     */
    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int pageSize,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "all") String tab) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }

        int safeSize = Math.max(1, Math.min(pageSize, 50));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        String like = (keyword != null && !keyword.isBlank()) ? "%" + keyword.trim() + "%" : null;

        String t = tab == null ? "all" : tab.trim().toLowerCase();
        Map<String, Object> stats = queryStats(userId, like);

        String listSql;
        Object[] listArgs;
        String countSql;
        Object[] countArgs;

        switch (t) {
            case "draft" -> {
                listSql = """
                        SELECT 'draft' AS kind,
                               CAST(NULL AS CHAR(50)) AS video_id,
                               d.submission_id AS submission_id,
                               d.title, d.description, d.cover_url,
                               d.source_file,
                               d.update_time AS sort_key,
                               d.update_time,
                               CAST(NULL AS CHAR(20)) AS review_status,
                               CAST(NULL AS UNSIGNED) AS view_count,
                               CAST(NULL AS UNSIGNED) AS like_count
                        FROM video_drafts d
                        WHERE d.user_id=?
                          AND (? IS NULL OR d.title LIKE ?)
                        ORDER BY d.update_time DESC
                        LIMIT ? OFFSET ?
                        """;
                listArgs = new Object[]{userId, like, like, safeSize, offset};
                countSql = """
                        SELECT COUNT(*) FROM video_drafts d
                        WHERE d.user_id=?
                          AND (? IS NULL OR d.title LIKE ?)
                        """;
                countArgs = new Object[]{userId, like, like};
            }
            case "reviewing" -> {
                listSql = """
                        SELECT 'submission' AS kind,
                               CAST(NULL AS CHAR(50)) AS video_id,
                               s.submission_id,
                               s.title, s.description, s.cover_url,
                               s.source_file,
                               s.update_time AS sort_key,
                               s.update_time,
                               s.review_status,
                               CAST(NULL AS UNSIGNED) AS view_count,
                               CAST(NULL AS UNSIGNED) AS like_count
                        FROM video_submissions s
                        WHERE s.user_id=?
                          AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                          AND s.review_status IN ('PENDING','APPROVED')
                          AND (? IS NULL OR s.title LIKE ?)
                        ORDER BY s.update_time DESC
                        LIMIT ? OFFSET ?
                        """;
                listArgs = new Object[]{userId, like, like, safeSize, offset};
                countSql = """
                        SELECT COUNT(*) FROM video_submissions s
                        WHERE s.user_id=?
                          AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                          AND (s.review_status = 'PENDING' OR s.review_status = 'APPROVED')
                          AND (? IS NULL OR s.title LIKE ?)
                        """;
                countArgs = new Object[]{userId, like, like};
            }
            case "approved" -> {
                listSql = """
                        SELECT 'published' AS kind,
                               v.video_id,
                               CAST(NULL AS CHAR(50)) AS submission_id,
                               v.title, v.description, v.cover_url,
                               v.source_file,
                               COALESCE(v.update_time, v.create_time) AS sort_key,
                               v.update_time,
                               CAST(NULL AS CHAR(20)) AS review_status,
                               v.view_count, v.like_count
                        FROM videos v
                        WHERE v.user_id=?
                          AND (? IS NULL OR v.title LIKE ?)
                        ORDER BY sort_key DESC
                        LIMIT ? OFFSET ?
                        """;
                listArgs = new Object[]{userId, like, like, safeSize, offset};
                countSql = """
                        SELECT COUNT(*) FROM videos v
                        WHERE v.user_id=?
                          AND (? IS NULL OR v.title LIKE ?)
                        """;
                countArgs = new Object[]{userId, like, like};
            }
            case "rejected" -> {
                listSql = """
                        SELECT 'submission' AS kind,
                               CAST(NULL AS CHAR(50)) AS video_id,
                               s.submission_id,
                               s.title, s.description, s.cover_url,
                               s.source_file,
                               s.update_time AS sort_key,
                               s.update_time,
                               s.review_status,
                               CAST(NULL AS UNSIGNED) AS view_count,
                               CAST(NULL AS UNSIGNED) AS like_count
                        FROM video_submissions s
                        WHERE s.user_id=?
                          AND s.review_status = 'REJECTED'
                          AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                          AND (? IS NULL OR s.title LIKE ?)
                        ORDER BY s.update_time DESC
                        LIMIT ? OFFSET ?
                        """;
                listArgs = new Object[]{userId, like, like, safeSize, offset};
                countSql = """
                        SELECT COUNT(*) FROM video_submissions s
                        WHERE s.user_id=?
                          AND s.review_status = 'REJECTED'
                          AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                          AND (? IS NULL OR s.title LIKE ?)
                        """;
                countArgs = new Object[]{userId, like, like};
            }
            default -> {
                // UNION 各分支字符串列必须统一 collation，否则 MySQL 报 1271 Illegal mix of collations
                listSql = """
                        SELECT * FROM (
                            SELECT CONVERT('published' USING utf8mb4) COLLATE utf8mb4_unicode_ci AS kind,
                                   CONVERT(v.video_id USING utf8mb4) COLLATE utf8mb4_unicode_ci AS video_id,
                                   CONVERT(CAST(NULL AS CHAR(50)) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS submission_id,
                                   CONVERT(IFNULL(v.title,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS title,
                                   CONVERT(IFNULL(v.description,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS description,
                                   CONVERT(IFNULL(v.cover_url,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cover_url,
                                   CONVERT(IFNULL(v.source_file,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS source_file,
                                   COALESCE(v.update_time, v.create_time) AS sort_key,
                                   v.update_time,
                                   CONVERT(CAST(NULL AS CHAR(20)) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS review_status,
                                   v.view_count, v.like_count
                            FROM videos v WHERE v.user_id=?
                            UNION ALL
                            SELECT CONVERT('draft' USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(CAST(NULL AS CHAR(50)) USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(d.submission_id USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(d.title,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(d.description,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(d.cover_url,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(d.source_file,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS source_file,
                                   d.update_time, d.update_time,
                                   CONVERT(CAST(NULL AS CHAR(20)) USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CAST(NULL AS UNSIGNED), CAST(NULL AS UNSIGNED)
                            FROM video_drafts d WHERE d.user_id=?
                            UNION ALL
                            SELECT CONVERT('submission' USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(CAST(NULL AS CHAR(50)) USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(s.submission_id USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(s.title,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(s.description,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(s.cover_url,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CONVERT(IFNULL(s.source_file,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS source_file,
                                   s.update_time, s.update_time,
                                   CONVERT(s.review_status USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                                   CAST(NULL AS UNSIGNED), CAST(NULL AS UNSIGNED)
                            FROM video_submissions s
                            WHERE s.user_id=?
                              AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                              AND s.review_status IN ('PENDING','APPROVED','REJECTED')
                        ) merged
                        WHERE (? IS NULL OR merged.title LIKE ?)
                        ORDER BY sort_key DESC
                        LIMIT ? OFFSET ?
                        """;
                listArgs = new Object[]{userId, userId, userId, like, like, safeSize, offset};
                countSql = """
                        SELECT COUNT(*) FROM (
                            SELECT CONVERT(IFNULL(v.title,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS t
                            FROM videos v WHERE v.user_id=?
                            UNION ALL
                            SELECT CONVERT(IFNULL(d.title,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS t
                            FROM video_drafts d WHERE d.user_id=?
                            UNION ALL
                            SELECT CONVERT(IFNULL(s.title,'') USING utf8mb4) COLLATE utf8mb4_unicode_ci AS t
                            FROM video_submissions s
                            WHERE s.user_id=?
                              AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                              AND s.review_status IN ('PENDING','APPROVED','REJECTED')
                        ) c
                        WHERE (? IS NULL OR c.t LIKE ?)
                        """;
                countArgs = new Object[]{userId, userId, userId, like, like};
            }
        }

        List<Map<String, Object>> list = jdbcTemplate.queryForList(listSql, listArgs);
        resolveCoverUrls(list);
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, countArgs);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", safePage);
        data.put("pageSize", safeSize);
        data.put("total", total == null ? 0 : total);
        data.put("stats", stats);
        data.put("tab", t);

        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    /**
     * videos / 草稿 / 投稿 里 cover_url 常仅存文件名，需与 source_file 组合成与视频详情一致的 /local-videos/... 地址
     */
    private void resolveCoverUrls(List<Map<String, Object>> list) {
        if (list == null) {
            return;
        }
        for (Map<String, Object> row : list) {
            Object sf = row.get("source_file");
            Object cv = row.get("cover_url");
            String url = localVideoService.buildCoverPublicUrl(
                    sf != null ? sf.toString() : null,
                    cv != null ? cv.toString() : null
            );
            if (StringUtils.hasText(url)) {
                row.put("cover_url", url);
            }
            row.remove("source_file");
        }
    }

    private Map<String, Object> queryStats(Long userId, String like) {
        Long draft = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM video_drafts d WHERE d.user_id=? AND (? IS NULL OR d.title LIKE ?)",
                Long.class, userId, like, like);
        Long reviewing = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM video_submissions s
                WHERE s.user_id=?
                  AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                  AND (s.review_status = 'PENDING' OR s.review_status = 'APPROVED')
                  AND (? IS NULL OR s.title LIKE ?)
                """,
                Long.class, userId, like, like);
        Long approved = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM videos v WHERE v.user_id=? AND (? IS NULL OR v.title LIKE ?)",
                Long.class, userId, like, like);
        Long rejected = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM video_submissions s
                WHERE s.user_id=?
                  AND s.review_status = 'REJECTED'
                  AND (s.published_video_id IS NULL OR TRIM(s.published_video_id) = '')
                  AND (? IS NULL OR s.title LIKE ?)
                """,
                Long.class, userId, like, like);
        long d = draft != null ? draft : 0;
        long r = reviewing != null ? reviewing : 0;
        long a = approved != null ? approved : 0;
        long j = rejected != null ? rejected : 0;
        Map<String, Object> m = new HashMap<>();
        m.put("draft", d);
        m.put("reviewing", r);
        m.put("approved", a);
        m.put("rejected", j);
        m.put("all", d + r + a + j);
        return m;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                }
                if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return null;
    }
}
