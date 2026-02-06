package videobackend.video.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import videobackend.video.model.FeedItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FeedService {

    private static final Logger log = LoggerFactory.getLogger(FeedService.class);
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FeedService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 创建动态
     */
    public FeedItem createFeed(Long userId, String title, String content, List<String> images) {
        String imagesJson = null;
        if (images != null && !images.isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(images);
            } catch (Exception e) {
                // 忽略JSON序列化错误
            }
        }

        String sql = """
                INSERT INTO feeds (user_id, title, content, images, like_count, comment_count, share_count, status)
                VALUES (?, ?, ?, ?, 0, 0, 0, 1)
                """;
        jdbcTemplate.update(sql, userId, title, content, imagesJson);

        // 获取刚插入的动态ID
        String getIdSql = "SELECT LAST_INSERT_ID()";
        Long feedId = jdbcTemplate.queryForObject(getIdSql, Long.class);

        // 返回创建的动态
        return getFeedById(feedId);
    }

    /**
     * 获取动态详情
     */
    public FeedItem getFeedById(Long feedId) {
        String sql = """
                SELECT f.id, f.user_id, f.title, f.content, f.images, f.like_count, f.comment_count, f.share_count, f.create_time,
                       u.username AS uploader_name, u.avatar AS uploader_avatar, u.id AS uploader_id
                FROM feeds f
                LEFT JOIN users u ON f.user_id = u.id
                WHERE f.id = ? AND f.status = 1
                """;
        List<FeedItem> list = jdbcTemplate.query(sql, (rs, i) -> mapToFeed(rs), feedId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 获取关注用户的动态列表（分页）
     * 包含自己发布的动态
     */
    public List<FeedItem> listPageByFollowing(Long userId, Long followingId, int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        log.debug("查询关注用户动态: userId={}, followingId={}, page={}, pageSize={}", userId, followingId, page, pageSize);

        // 如果指定了followingId，只返回该用户的动态
        if (followingId != null) {
            String sql = """
                    SELECT f.id, f.user_id, f.title, f.content, f.images, f.like_count, f.comment_count, f.share_count, f.create_time,
                           u.username AS uploader_name, u.avatar AS uploader_avatar, u.id AS uploader_id
                    FROM feeds f
                    LEFT JOIN users u ON f.user_id = u.id
                    WHERE f.user_id = ?
                      AND f.status = 1
                    ORDER BY f.create_time DESC
                    LIMIT ? OFFSET ?
                    """;
            List<FeedItem> result = jdbcTemplate.query(sql, (rs, i) -> mapToFeed(rs), followingId, safeSize, offset);
            log.debug("查询指定用户动态结果数量: {}", result.size());
            return result;
        }

        // 返回自己发布的动态 + 关注用户的动态
        String sql = """
                SELECT f.id, f.user_id, f.title, f.content, f.images, f.like_count, f.comment_count, f.share_count, f.create_time,
                       u.username AS uploader_name, u.avatar AS uploader_avatar, u.id AS uploader_id
                FROM feeds f
                LEFT JOIN users u ON f.user_id = u.id
                WHERE f.status = 1
                  AND (
                    f.user_id = ?
                    OR f.user_id IN (
                      SELECT fan.following_id 
                      FROM fans fan 
                      WHERE fan.follower_id = ?
                    )
                  )
                ORDER BY f.create_time DESC
                LIMIT ? OFFSET ?
                """;
        List<FeedItem> result = jdbcTemplate.query(sql, (rs, i) -> mapToFeed(rs), userId, userId, safeSize, offset);
        log.debug("查询关注用户动态结果数量: {}", result.size());
        return result;
    }

    /**
     * 获取关注用户的动态总数
     * 包含自己发布的动态
     */
    public long countByFollowing(Long userId, Long followingId) {
        // 如果指定了followingId，只返回该用户的动态数
        if (followingId != null) {
            String sql = "SELECT COUNT(*) FROM feeds WHERE user_id = ? AND status = 1";
            Long total = jdbcTemplate.queryForObject(sql, Long.class, followingId);
            return total == null ? 0 : total;
        }

        // 返回自己发布的动态 + 关注用户的动态总数
        String sql = """
                SELECT COUNT(*)
                FROM feeds f
                WHERE f.status = 1
                  AND (
                    f.user_id = ?
                    OR f.user_id IN (
                      SELECT fan.following_id 
                      FROM fans fan 
                      WHERE fan.follower_id = ?
                    )
                  )
                """;
        Long total = jdbcTemplate.queryForObject(sql, Long.class, userId, userId);
        return total == null ? 0 : total;
    }

    /**
     * 删除动态（软删除）
     */
    public boolean deleteFeed(Long feedId, Long userId) {
        String sql = "UPDATE feeds SET status = 0 WHERE id = ? AND user_id = ?";
        int rows = jdbcTemplate.update(sql, feedId, userId);
        return rows > 0;
    }

    /**
     * 点赞动态
     */
    public boolean likeFeed(Long feedId, Long userId) {
        // 检查是否已点赞
        String checkSql = "SELECT COUNT(*) FROM feed_likes WHERE feed_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, feedId, userId);
        if (count != null && count > 0) {
            return false; // 已点赞
        }

        // 添加点赞记录
        String insertSql = "INSERT INTO feed_likes (feed_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, feedId, userId);

        // 更新点赞数
        String updateSql = "UPDATE feeds SET like_count = like_count + 1 WHERE id = ?";
        jdbcTemplate.update(updateSql, feedId);
        return true;
    }

    /**
     * 取消点赞
     */
    public boolean unlikeFeed(Long feedId, Long userId) {
        String deleteSql = "DELETE FROM feed_likes WHERE feed_id = ? AND user_id = ?";
        int rows = jdbcTemplate.update(deleteSql, feedId, userId);
        if (rows > 0) {
            // 更新点赞数
            String updateSql = "UPDATE feeds SET like_count = GREATEST(like_count - 1, 0) WHERE id = ?";
            jdbcTemplate.update(updateSql, feedId);
            return true;
        }
        return false;
    }

    /**
     * 映射 ResultSet 到 FeedItem
     */
    private FeedItem mapToFeed(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long userId = rs.getLong("user_id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        
        // 解析图片JSON
        List<String> images = new ArrayList<>();
        String imagesJson = rs.getString("images");
        if (imagesJson != null && !imagesJson.trim().isEmpty()) {
            try {
                images = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                // 忽略JSON解析错误
            }
        }

        Long likeCount = rs.getObject("like_count") != null ? rs.getLong("like_count") : 0L;
        Long commentCount = rs.getObject("comment_count") != null ? rs.getLong("comment_count") : 0L;
        Long shareCount = rs.getObject("share_count") != null ? rs.getLong("share_count") : 0L;

        // 格式化时间
        Timestamp createTime = rs.getTimestamp("create_time");
        String createTimeStr = createTime != null ? DATE_FORMATTER.format(createTime.toLocalDateTime()) : null;

        String uploaderName = rs.getString("uploader_name");
        String uploaderAvatar = rs.getString("uploader_avatar");
        Long uploaderId = rs.getObject("uploader_id") != null ? rs.getLong("uploader_id") : null;

        return new FeedItem(
                id, userId, title, content, images, likeCount, commentCount, shareCount,
                createTimeStr, uploaderName, uploaderAvatar, uploaderId
        );
    }
}

