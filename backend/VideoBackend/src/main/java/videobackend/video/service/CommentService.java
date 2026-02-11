package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.CommentItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CommentService {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CommentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 分页获取视频的顶层评论列表（不包含回复）
     * @param sort 排序方式：time 按时间倒序；hot 按热度（点赞数优先，其次时间）
     */
    public List<CommentItem> listComments(String videoId, int page, int pageSize, String sort) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String orderBy;
        if ("hot".equalsIgnoreCase(sort)) {
            orderBy = "c.like_count DESC, c.create_time DESC";
        } else {
            orderBy = "c.create_time DESC";
        }

        String sql = """
                SELECT c.id, c.video_id, c.user_id, c.content, c.parent_id, c.like_count, c.create_time,
                       u.username, u.avatar
                FROM comments c
                LEFT JOIN users u ON c.user_id = u.id
                WHERE c.video_id = ?
                  AND c.status = 1
                  AND c.parent_id IS NULL
                ORDER BY %s
                LIMIT ? OFFSET ?
                """.formatted(orderBy);
        return jdbcTemplate.query(sql, (rs, i) -> mapToCommentItem(rs), videoId, safeSize, offset);
    }

    /**
     * 统计视频的顶层评论总数
     */
    public long countComments(String videoId) {
        String sql = """
                SELECT COUNT(*)
                FROM comments
                WHERE video_id = ?
                  AND status = 1
                  AND parent_id IS NULL
                """;
        Long total = jdbcTemplate.queryForObject(sql, Long.class, videoId);
        return total == null ? 0L : total;
    }

    /**
     * 新增评论（支持顶层评论或回复）
     */
    @Transactional
    public CommentItem addComment(Long userId, String videoId, String content, Long parentId) {
        String insertSql = """
                INSERT INTO comments (video_id, user_id, content, parent_id, like_count, status, create_time, update_time)
                VALUES (?, ?, ?, ?, 0, 1, NOW(), NOW())
                """;
        jdbcTemplate.update(insertSql, videoId, userId, content, parentId);

        // 获取自增ID
        Long commentId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (commentId == null) {
            throw new RuntimeException("创建评论失败：无法获取评论ID");
        }

        // 返回完整的评论信息
        return getCommentById(commentId);
    }

    /**
     * 获取某个评论下的所有回复（按时间正序）
     */
    public List<CommentItem> listReplies(String videoId, Long parentId) {
        String sql = """
                SELECT c.id, c.video_id, c.user_id, c.content, c.parent_id, c.like_count, c.create_time,
                       u.username, u.avatar
                FROM comments c
                LEFT JOIN users u ON c.user_id = u.id
                WHERE c.video_id = ?
                  AND c.status = 1
                  AND c.parent_id = ?
                ORDER BY c.create_time ASC
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToCommentItem(rs), videoId, parentId);
    }

    /**
     * 根据ID获取评论
     */
    public CommentItem getCommentById(Long id) {
        String sql = """
                SELECT c.id, c.video_id, c.user_id, c.content, c.parent_id, c.like_count, c.create_time,
                       u.username, u.avatar
                FROM comments c
                LEFT JOIN users u ON c.user_id = u.id
                WHERE c.id = ?
                """;
        List<CommentItem> list = jdbcTemplate.query(sql, (rs, i) -> mapToCommentItem(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 点赞评论
     */
    @Transactional
    public boolean likeComment(Long userId, Long commentId) {
        // 检查是否已点赞
        String checkSql = "SELECT COUNT(*) FROM comment_likes WHERE comment_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, commentId, userId);
        if (count != null && count > 0) {
            return false;
        }

        // 添加点赞记录
        String insertSql = "INSERT INTO comment_likes (comment_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, commentId, userId);

        // 更新点赞数
        String updateSql = "UPDATE comments SET like_count = like_count + 1 WHERE id = ?";
        jdbcTemplate.update(updateSql, commentId);
        return true;
    }

    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlikeComment(Long userId, Long commentId) {
        String deleteSql = "DELETE FROM comment_likes WHERE comment_id = ? AND user_id = ?";
        int rows = jdbcTemplate.update(deleteSql, commentId, userId);
        if (rows > 0) {
            String updateSql = "UPDATE comments SET like_count = GREATEST(like_count - 1, 0) WHERE id = ?";
            jdbcTemplate.update(updateSql, commentId);
            return true;
        }
        return false;
    }

    /**
     * 获取评论点赞数
     */
    public long getLikeCount(Long commentId) {
        String sql = "SELECT like_count FROM comments WHERE id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, commentId);
        return count == null ? 0L : count;
    }

    private CommentItem mapToCommentItem(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String videoId = rs.getString("video_id");
        Long userId = rs.getLong("user_id");
        String content = rs.getString("content");
        Long parentId = rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null;
        Long likeCount = rs.getObject("like_count") != null ? rs.getLong("like_count") : 0L;
        Timestamp createTime = rs.getTimestamp("create_time");
        String createTimeStr = createTime != null
                ? DATE_FORMATTER.format(createTime.toLocalDateTime())
                : DATE_FORMATTER.format(LocalDateTime.now());
        String username = rs.getString("username");
        String avatar = rs.getString("avatar");

        return new CommentItem(
                id,
                videoId,
                userId,
                username,
                avatar,
                content,
                parentId,
                likeCount,
                createTimeStr
        );
    }
}


