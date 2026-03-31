package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AvatarSubmissionService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_REPLACED = "REPLACED";

    private final JdbcTemplate jdbcTemplate;
    private final AvatarStorageService avatarStorageService;
    private final MediaUrlResolver mediaUrlResolver;

    public AvatarSubmissionService(JdbcTemplate jdbcTemplate,
                                   AvatarStorageService avatarStorageService,
                                   MediaUrlResolver mediaUrlResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.avatarStorageService = avatarStorageService;
        this.mediaUrlResolver = mediaUrlResolver;
    }

    @Transactional
    public Map<String, Object> submitAvatar(Long userId, MultipartFile file) throws IOException {
        if (userId == null) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传头像文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("头像必须是图片文件");
        }

        // 先存储图片，得到可访问 URL（/avatars/** 或 CDN 绝对 URL）
        String avatarUrl = avatarStorageService.storeAvatar(userId, file);

        // 将旧的待审头像标记为 REPLACED，避免同一用户多个 pending 堆积导致审核歧义
        jdbcTemplate.update("""
                        UPDATE avatar_submissions
                        SET status=?,
                            update_time=NOW()
                        WHERE user_id=?
                          AND status=?
                        """,
                STATUS_REPLACED,
                userId,
                STATUS_PENDING
        );

        jdbcTemplate.update("""
                        INSERT INTO avatar_submissions
                        (user_id, avatar_url, status, create_time, update_time)
                        VALUES (?, ?, ?, NOW(), NOW())
                        """,
                userId,
                avatarUrl,
                STATUS_PENDING
        );

        return Map.of(
                "status", STATUS_PENDING,
                "pendingAvatar", mediaUrlResolver.resolveAvatar(avatarUrl)
        );
    }

    /**
     * 返回用户最新一条头像审核记录（若无记录则为空 Map）。
     */
    public Map<String, Object> getLatestForUser(Long userId) {
        if (userId == null) {
            return Map.of();
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap("""
                    SELECT id, user_id, avatar_url, status, review_comment, review_time, create_time, update_time
                    FROM avatar_submissions
                    WHERE user_id=?
                    ORDER BY create_time DESC, id DESC
                    LIMIT 1
                    """, userId);
            String avatarUrl = row.get("avatar_url") == null ? null : String.valueOf(row.get("avatar_url"));
            row.put("avatar_url", mediaUrlResolver.resolveAvatar(avatarUrl));
            return row;
        } catch (Exception ignore) {
            return Map.of();
        }
    }

    public Map<String, Object> listPending(int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        List<Map<String, Object>> list = jdbcTemplate.queryForList("""
                SELECT s.id,
                       s.user_id,
                       s.avatar_url,
                       s.status,
                       s.create_time,
                       u.username,
                       u.avatar AS current_avatar
                FROM avatar_submissions s
                LEFT JOIN users u ON s.user_id = u.id
                WHERE s.status = ?
                ORDER BY s.create_time DESC, s.id DESC
                LIMIT ? OFFSET ?
                """, STATUS_PENDING, safeSize, offset);

        list.forEach(row -> {
            String pending = row.get("avatar_url") == null ? null : String.valueOf(row.get("avatar_url"));
            String current = row.get("current_avatar") == null ? null : String.valueOf(row.get("current_avatar"));
            row.put("avatar_url", mediaUrlResolver.resolveAvatar(pending));
            row.put("current_avatar", mediaUrlResolver.resolveAvatar(current));
        });

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM avatar_submissions WHERE status=?",
                Long.class,
                STATUS_PENDING
        );
        return Map.of(
                "list", list,
                "page", safePage,
                "pageSize", safeSize,
                "total", total == null ? 0 : total
        );
    }

    @Transactional
    public void approve(Long submissionId, Long adminId, String comment) {
        if (submissionId == null || submissionId <= 0) {
            throw new IllegalArgumentException("审核记录ID无效");
        }
        if (adminId == null) {
            throw new IllegalArgumentException("无权限");
        }

        Map<String, Object> row;
        try {
            row = jdbcTemplate.queryForMap("""
                    SELECT id, user_id, avatar_url, status
                    FROM avatar_submissions
                    WHERE id=?
                    """, submissionId);
        } catch (Exception e) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        String status = row.get("status") == null ? "" : String.valueOf(row.get("status"));
        if (!STATUS_PENDING.equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("状态不可审核（可能已被处理）");
        }
        Long userId = row.get("user_id") instanceof Number ? ((Number) row.get("user_id")).longValue() : null;
        String avatarUrl = row.get("avatar_url") == null ? null : String.valueOf(row.get("avatar_url"));
        if (userId == null || !StringUtils.hasText(avatarUrl)) {
            throw new IllegalArgumentException("记录数据不完整，无法审核");
        }

        int updated = jdbcTemplate.update("""
                        UPDATE avatar_submissions
                        SET status=?,
                            reviewer_id=?,
                            review_comment=?,
                            review_time=NOW(),
                            update_time=NOW()
                        WHERE id=?
                          AND status=?
                        """,
                STATUS_APPROVED,
                adminId,
                StringUtils.hasText(comment) ? comment.trim() : null,
                submissionId,
                STATUS_PENDING
        );
        if (updated <= 0) {
            throw new IllegalArgumentException("审核失败（可能已被处理）");
        }

        // 审核通过后，写回 users.avatar
        jdbcTemplate.update("""
                UPDATE users
                SET avatar=?,
                    update_time=NOW()
                WHERE id=?
                """, avatarUrl, userId);
    }

    @Transactional
    public void reject(Long submissionId, Long adminId, String comment) {
        if (submissionId == null || submissionId <= 0) {
            throw new IllegalArgumentException("审核记录ID无效");
        }
        if (adminId == null) {
            throw new IllegalArgumentException("无权限");
        }
        if (!StringUtils.hasText(comment)) {
            throw new IllegalArgumentException("驳回原因不能为空");
        }

        int updated = jdbcTemplate.update("""
                        UPDATE avatar_submissions
                        SET status=?,
                            reviewer_id=?,
                            review_comment=?,
                            review_time=NOW(),
                            update_time=NOW()
                        WHERE id=?
                          AND status=?
                        """,
                STATUS_REJECTED,
                adminId,
                comment.trim(),
                submissionId,
                STATUS_PENDING
        );
        if (updated <= 0) {
            throw new IllegalArgumentException("审核记录不存在或状态不可审核（可能已被处理）");
        }
    }
}

