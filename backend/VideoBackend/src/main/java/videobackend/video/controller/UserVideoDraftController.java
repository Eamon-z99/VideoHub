package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

/**
 * 内容管理-草稿箱：列表/详情/删除
 *
 * 注意：草稿的保存/提交由 UserVideoSubmissionController 的 update(submitNow=0/1) 完成，
 * 这里主要提供内容管理页的数据读取与删除。
 */
@RestController
@RequestMapping("/api/db/video-drafts")
public class UserVideoDraftController {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;

    public UserVideoDraftController(JdbcTemplate jdbcTemplate, JwtUtil jwtUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int pageSize,
                                  @RequestParam(required = false) String keyword) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        int safeSize = Math.max(1, Math.min(pageSize, 50));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String like = (keyword != null && !keyword.isBlank()) ? "%" + keyword.trim() + "%" : null;

        List<Map<String, Object>> list = jdbcTemplate.queryForList("""
                        SELECT d.submission_id,
                               d.title,
                               d.description,
                               d.cover_url,
                               d.duration,
                               d.update_time
                        FROM video_drafts d
                        WHERE d.user_id=?
                          AND (? IS NULL OR d.title LIKE ?)
                        ORDER BY d.update_time DESC
                        LIMIT ? OFFSET ?
                        """, userId, like, like, safeSize, offset);

        Long total = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM video_drafts d
                        WHERE d.user_id=?
                          AND (? IS NULL OR d.title LIKE ?)
                        """, Long.class, userId, like, like);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "list", list,
                        "page", safePage,
                        "pageSize", safeSize,
                        "total", total == null ? 0 : total
                )
        ));
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<?> detail(HttpServletRequest request, @PathVariable String submissionId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap("""
                    SELECT submission_id, user_id, title, description,
                           copyright, `partition`, tags, duration,
                           cover_url, storage_path, source_file, file_size,
                           schedule_enabled, schedule_publish_at,
                           collection_enabled, collection_name,
                           allow_second_creation, commercial_promotion,
                           create_time, update_time
                    FROM video_drafts
                    WHERE submission_id=? AND user_id=?
                    """, submissionId, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", row));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "草稿不存在"));
        }
    }

    @DeleteMapping("/{submissionId}")
    public ResponseEntity<?> delete(HttpServletRequest request, @PathVariable String submissionId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        int deleted = jdbcTemplate.update("DELETE FROM video_drafts WHERE submission_id=? AND user_id=?", submissionId, userId);
        // 同步把 submissions 里的 DRAFT 一并删掉（避免磁盘文件孤儿：后续可加异步清理）
        jdbcTemplate.update("DELETE FROM video_submissions WHERE submission_id=? AND user_id=? AND review_status='DRAFT'", submissionId, userId);
        return ResponseEntity.ok(Map.of("success", true, "deleted", deleted));
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

