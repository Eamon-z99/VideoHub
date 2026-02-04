package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FollowService {

    private final JdbcTemplate jdbcTemplate;

    public FollowService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 关注用户
     */
    public boolean followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("不能关注自己");
        }

        // 检查是否已关注
        String checkSql = "SELECT COUNT(*) FROM fans WHERE follower_id = ? AND following_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, followerId, followingId);
        if (count != null && count > 0) {
            return false; // 已关注
        }

        // 添加关注
        String insertSql = "INSERT INTO fans (follower_id, following_id) VALUES (?, ?)";
        int rows = jdbcTemplate.update(insertSql, followerId, followingId);
        return rows > 0;
    }

    /**
     * 取消关注
     */
    public boolean unfollowUser(Long followerId, Long followingId) {
        String sql = "DELETE FROM fans WHERE follower_id = ? AND following_id = ?";
        int rows = jdbcTemplate.update(sql, followerId, followingId);
        return rows > 0;
    }

    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        String sql = "SELECT COUNT(*) FROM fans WHERE follower_id = ? AND following_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, followerId, followingId);
        return count != null && count > 0;
    }

    /**
     * 获取用户的关注列表（我关注的用户ID列表）
     */
    public List<Long> getFollowingIds(Long userId) {
        String sql = "SELECT following_id FROM fans WHERE follower_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    /**
     * 获取用户的粉丝列表（关注我的用户ID列表）
     */
    public List<Long> getFollowerIds(Long userId) {
        String sql = "SELECT follower_id FROM fans WHERE following_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    /**
     * 获取关注数量
     */
    public long getFollowingCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM fans WHERE follower_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0;
    }

    /**
     * 获取粉丝数量
     */
    public long getFollowerCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM fans WHERE following_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0;
    }

    /**
     * 获取用户信息（包含关注和粉丝数）
     */
    public Map<String, Object> getUserStats(Long userId) {
        long followingCount = getFollowingCount(userId);
        long followerCount = getFollowerCount(userId);
        
        // 获取视频数量
        String videoCountSql = "SELECT COUNT(*) FROM videos WHERE user_id = ?";
        Long videoCount = jdbcTemplate.queryForObject(videoCountSql, Long.class, userId);
        
        return Map.of(
            "followingCount", followingCount,
            "followerCount", followerCount,
            "videoCount", videoCount != null ? videoCount : 0
        );
    }

    /**
     * 获取关注用户的详细信息列表（包含头像、名称等）
     */
    public List<Map<String, Object>> getFollowingUsers(Long userId) {
        String sql = """
                SELECT u.id, u.username, u.avatar, u.account
                FROM fans f
                INNER JOIN users u ON f.following_id = u.id
                WHERE f.follower_id = ?
                ORDER BY f.create_time DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> user = new java.util.HashMap<>();
            user.put("id", rs.getLong("id"));
            user.put("username", rs.getString("username"));
            user.put("avatar", rs.getString("avatar"));
            user.put("account", rs.getString("account"));
            return user;
        }, userId);
    }
}

