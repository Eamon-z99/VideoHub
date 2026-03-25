package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.VideoComplaintItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class VideoComplaintService {

    private final JdbcTemplate jdbcTemplate;
    private final MessageService messageService;

    public VideoComplaintService(JdbcTemplate jdbcTemplate, MessageService messageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.messageService = messageService;
    }

    @Transactional
    public long createComplaint(Long reporterUserId,
                                 String videoId,
                                 String category,
                                 String otherDetail,
                                 String evidenceUrlsJson) {
        String sql = """
                INSERT INTO video_complaints
                (video_id, reporter_user_id, category, other_detail, evidence_urls, status, created_time, update_time)
                VALUES (?, ?, ?, ?, ?, 0, NOW(), NOW())
                """;
        jdbcTemplate.update(sql, videoId, reporterUserId, category, otherDetail, evidenceUrlsJson);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id == null ? 0L : id;
    }

    public List<VideoComplaintItem> listPending(int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String sql = """
                SELECT vc.id,
                       vc.video_id,
                       v.title AS video_title,
                       vc.reporter_user_id,
                       ru.username AS reporter_username,
                       vc.category,
                       vc.other_detail,
                       vc.evidence_urls,
                       vc.status,
                       vc.handler_admin_id,
                       vc.handler_action,
                       vc.handler_note,
                       vc.created_time
                FROM video_complaints vc
                JOIN videos v ON v.video_id = vc.video_id
                JOIN users ru ON ru.id = vc.reporter_user_id
                WHERE vc.status = 0
                ORDER BY vc.created_time DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(sql, (rs, i) -> mapToItem(rs), safeSize, offset);
    }

    public long countPending() {
        String sql = "SELECT COUNT(*) FROM video_complaints WHERE status = 0";
        Long total = jdbcTemplate.queryForObject(sql, Long.class);
        return total == null ? 0L : total;
    }

    @Transactional
    public void processComplaint(Long adminId, Long complaintId, String action, String handlerNote) {
        // 读取必要信息：举报人/作品作者/举报类型
        String querySql = """
                SELECT vc.id,
                       vc.video_id,
                       vc.reporter_user_id,
                       v.user_id AS author_user_id,
                       vc.category,
                       COALESCE(vc.other_detail, '') AS other_detail,
                       vc.status
                FROM video_complaints vc
                JOIN videos v ON v.video_id = vc.video_id
                WHERE vc.id = ?
                """;

        var row = jdbcTemplate.queryForMap(querySql, complaintId);
        Integer status = row.get("status") instanceof Number ? ((Number) row.get("status")).intValue() : 0;
        if (status != 0) {
            return; // 已处理直接返回
        }

        String videoId = String.valueOf(row.get("video_id"));
        Long reporterUserId = ((Number) row.get("reporter_user_id")).longValue();
        Long authorUserId = ((Number) row.get("author_user_id")).longValue();
        String category = String.valueOf(row.get("category"));
        String otherDetail = String.valueOf(row.get("other_detail"));

        String normalizedAction = action == null ? "" : action.trim().toLowerCase();
        // warn/takedown/dismiss
        int newStatus;
        String handlerAction;

        boolean takedown = false;
        if ("warn".equals(normalizedAction)) {
            newStatus = 1;
            handlerAction = "WARN";
        } else if ("takedown".equals(normalizedAction) || "down".equals(normalizedAction)) {
            newStatus = 1;
            handlerAction = "TAKEDOWN";
            takedown = true;
        } else {
            newStatus = 2;
            handlerAction = "DISMISS";
        }

        // 更新举报记录
        String updateSql = """
                UPDATE video_complaints
                SET status = ?,
                    handler_admin_id = ?,
                    handler_action = ?,
                    handler_note = ?,
                    update_time = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(updateSql, newStatus, adminId, handlerAction, handlerNote, complaintId);

        // 对作者进行通知（警示/下架都通知）
        String msg = buildAdminMessage(handlerAction, category, otherDetail, handlerNote);
        if (authorUserId != null) {
            messageService.sendMessage(reporterUserId, authorUserId, msg);
        }

        if (takedown) {
            // 下架：把 videos.status 设置为 DOWN
            jdbcTemplate.update("""
                    UPDATE videos
                    SET status = 'DOWN',
                        update_time = NOW()
                    WHERE video_id = ?
                    """, videoId);
        }
    }

    private String buildAdminMessage(String handlerAction, String category, String otherDetail, String handlerNote) {
        String detail = (otherDetail != null && !otherDetail.isBlank()) ? otherDetail.trim() : "";
        String base = "你的作品已收到举报，管理员处理结果：";
        if ("TAKEDOWN".equalsIgnoreCase(handlerAction)) {
            base += "已下架";
        } else if ("WARN".equalsIgnoreCase(handlerAction)) {
            base += "已警示";
        } else {
            base += "不予处理";
        }
        base += "。举报类型：" + category;
        if (!detail.isBlank()) base += "，其他说明：" + detail;
        if (handlerNote != null && !handlerNote.isBlank()) base += "（备注：" + handlerNote.trim() + "）";
        base += "。";
        return base;
    }

    private VideoComplaintItem mapToItem(ResultSet rs) throws SQLException {
        return new VideoComplaintItem(
                rs.getLong("id"),
                rs.getString("video_id"),
                rs.getString("video_title"),
                rs.getLong("reporter_user_id"),
                rs.getString("reporter_username"),
                rs.getString("category"),
                rs.getString("other_detail"),
                rs.getObject("evidence_urls") != null ? rs.getString("evidence_urls") : null,
                rs.getInt("status"),
                rs.getObject("handler_admin_id") != null ? rs.getLong("handler_admin_id") : null,
                rs.getString("handler_action"),
                rs.getString("handler_note"),
                rs.getObject("created_time") != null ? rs.getTimestamp("created_time").toString() : null
        );
    }
}

