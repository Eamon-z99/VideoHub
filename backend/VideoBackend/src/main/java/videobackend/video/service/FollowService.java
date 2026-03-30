package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class FollowService {

    private final JdbcTemplate jdbcTemplate;
    private final MediaUrlResolver mediaUrlResolver;

    public FollowService(JdbcTemplate jdbcTemplate, MediaUrlResolver mediaUrlResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.mediaUrlResolver = mediaUrlResolver;
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
     * 某用户的关注列表。i_follow：当前浏览者 viewerId 是否已关注该用户（未登录时 viewerId 传 -1）。
     *
     * @param sort {@code recent}：按关注时间；{@code active}：按 list 主人访问对方主页的次数（profile_visits，按日累计条数）
     */
    public List<Map<String, Object>> getFollowingUsers(long profileUserId, long viewerId) {
        return getFollowingUsers(profileUserId, viewerId, "recent");
    }

    public List<Map<String, Object>> getFollowingUsers(long profileUserId, long viewerId, String sort) {
        boolean byVisit = sort != null && "active".equalsIgnoreCase(sort.trim());
        if (byVisit) {
            String sql = """
                    SELECT u.id, u.username, u.avatar, u.account, u.bio,
                           f.group_id,
                           fg.name AS group_name,
                           (SELECT COUNT(*) FROM fans f2 WHERE f2.following_id = u.id) AS follower_count,
                           CASE WHEN vf.id IS NULL THEN 0 ELSE 1 END AS i_follow
                    FROM fans f
                    INNER JOIN users u ON f.following_id = u.id
                    LEFT JOIN follow_group fg ON fg.id = f.group_id AND fg.user_id = f.follower_id
                    LEFT JOIN (
                        SELECT profile_user_id, COUNT(*) AS visit_cnt
                        FROM profile_visits
                        WHERE visitor_id = ?
                        GROUP BY profile_user_id
                    ) pv ON pv.profile_user_id = u.id
                    LEFT JOIN fans vf ON vf.follower_id = ? AND vf.following_id = u.id
                    WHERE f.follower_id = ?
                    ORDER BY COALESCE(pv.visit_cnt, 0) DESC, f.create_time DESC
                    """;
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapFollowingUserRow(rs), profileUserId, viewerId, profileUserId);
        }
        String sql = """
                SELECT u.id, u.username, u.avatar, u.account, u.bio,
                       f.group_id,
                       fg.name AS group_name,
                       (SELECT COUNT(*) FROM fans f2 WHERE f2.following_id = u.id) AS follower_count,
                       CASE WHEN vf.id IS NULL THEN 0 ELSE 1 END AS i_follow
                FROM fans f
                INNER JOIN users u ON f.following_id = u.id
                LEFT JOIN follow_group fg ON fg.id = f.group_id AND fg.user_id = f.follower_id
                LEFT JOIN fans vf ON vf.follower_id = ? AND vf.following_id = u.id
                WHERE f.follower_id = ?
                ORDER BY f.create_time DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFollowingUserRow(rs), viewerId, profileUserId);
    }

    private Map<String, Object> mapFollowingUserRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> user = new java.util.HashMap<>();
        user.put("id", rs.getLong("id"));
        user.put("username", rs.getString("username"));
        user.put("avatar", mediaUrlResolver.resolveAvatar(rs.getString("avatar")));
        user.put("account", rs.getString("account"));
        user.put("bio", rs.getString("bio"));
        long gid = rs.getLong("group_id");
        if (rs.wasNull()) {
            user.put("groupId", null);
        } else {
            user.put("groupId", gid);
        }
        user.put("groupName", rs.getString("group_name"));
        user.put("followerCount", rs.getLong("follower_count"));
        user.put("iFollow", rs.getInt("i_follow"));
        return user;
    }

    /**
     * 某用户的粉丝列表。i_follow：当前浏览者是否已关注该粉丝（用于「关注/已关注」按钮）。
     *
     * @param sort {@code recent}：按成为粉丝时间；{@code active}：按粉丝访问本主页次数（profile_visits）
     */
    public List<Map<String, Object>> getFollowerUsers(long profileUserId, long viewerId) {
        return getFollowerUsers(profileUserId, viewerId, "recent");
    }

    public List<Map<String, Object>> getFollowerUsers(long profileUserId, long viewerId, String sort) {
        boolean byVisit = sort != null && "active".equalsIgnoreCase(sort.trim());
        if (byVisit) {
            String sql = """
                    SELECT u.id, u.username, u.avatar, u.account, u.bio,
                           (SELECT COUNT(*) FROM fans f2 WHERE f2.following_id = u.id) AS follower_count,
                           CASE WHEN vf.id IS NULL THEN 0 ELSE 1 END AS i_follow
                    FROM fans f
                    INNER JOIN users u ON f.follower_id = u.id
                    LEFT JOIN (
                        SELECT visitor_id, COUNT(*) AS visit_cnt
                        FROM profile_visits
                        WHERE profile_user_id = ?
                        GROUP BY visitor_id
                    ) pv ON pv.visitor_id = u.id
                    LEFT JOIN fans vf ON vf.follower_id = ? AND vf.following_id = u.id
                    WHERE f.following_id = ?
                    ORDER BY COALESCE(pv.visit_cnt, 0) DESC, f.create_time DESC
                    """;
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapFollowerUserRow(rs), profileUserId, viewerId, profileUserId);
        }
        String sql = """
                SELECT u.id, u.username, u.avatar, u.account, u.bio,
                       (SELECT COUNT(*) FROM fans f2 WHERE f2.following_id = u.id) AS follower_count,
                       CASE WHEN vf.id IS NULL THEN 0 ELSE 1 END AS i_follow
                FROM fans f
                INNER JOIN users u ON f.follower_id = u.id
                LEFT JOIN fans vf ON vf.follower_id = ? AND vf.following_id = u.id
                WHERE f.following_id = ?
                ORDER BY f.create_time DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFollowerUserRow(rs), viewerId, profileUserId);
    }

    private Map<String, Object> mapFollowerUserRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> user = new java.util.HashMap<>();
        user.put("id", rs.getLong("id"));
        user.put("username", rs.getString("username"));
        user.put("avatar", mediaUrlResolver.resolveAvatar(rs.getString("avatar")));
        user.put("account", rs.getString("account"));
        user.put("bio", rs.getString("bio"));
        user.put("followerCount", rs.getLong("follower_count"));
        user.put("iFollow", rs.getInt("i_follow"));
        return user;
    }

    /**
     * 移除粉丝：删除对方对我的关注关系
     */
    public boolean removeFollower(Long userId, Long followerId) {
        String sql = "DELETE FROM fans WHERE follower_id = ? AND following_id = ?";
        int rows = jdbcTemplate.update(sql, followerId, userId);
        return rows > 0;
    }

    /**
     * 当前用户的关注分组列表（含每组人数）
     */
    public List<Map<String, Object>> listFollowGroups(long ownerUserId) {
        String sql = """
                SELECT fg.id, fg.name, fg.sort_order,
                       (SELECT COUNT(*) FROM fans f
                        WHERE f.follower_id = fg.user_id AND f.group_id = fg.id) AS member_count
                FROM follow_group fg
                WHERE fg.user_id = ?
                ORDER BY fg.sort_order ASC, fg.id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", rs.getLong("id"));
            m.put("name", rs.getString("name"));
            m.put("sortOrder", rs.getInt("sort_order"));
            m.put("memberCount", rs.getLong("member_count"));
            return m;
        }, ownerUserId);
    }

    /**
     * 新建关注分组
     */
    public Map<String, Object> createFollowGroup(long ownerUserId, String name) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) {
            throw new IllegalArgumentException("分组名称不能为空");
        }
        if (n.length() > 100) {
            throw new IllegalArgumentException("分组名称过长");
        }
        String dup = "SELECT COUNT(*) FROM follow_group WHERE user_id = ? AND name = ?";
        Integer c = jdbcTemplate.queryForObject(dup, Integer.class, ownerUserId, n);
        if (c != null && c > 0) {
            throw new IllegalArgumentException("已有同名分组");
        }
        String maxSql = "SELECT COALESCE(MAX(sort_order), -1) FROM follow_group WHERE user_id = ?";
        Integer maxOrder = jdbcTemplate.queryForObject(maxSql, Integer.class, ownerUserId);
        int next = (maxOrder != null ? maxOrder : -1) + 1;

        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO follow_group (user_id, name, sort_order) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, ownerUserId);
            ps.setString(2, n);
            ps.setInt(3, next);
            return ps;
        }, kh);
        Number key = kh.getKey();
        long id = key != null ? key.longValue() : 0L;
        return Map.of("id", id, "name", n, "sortOrder", next, "memberCount", 0L);
    }

    /**
     * 删除关注分组（fans.group_id 外键 ON DELETE SET NULL）
     */
    public boolean deleteFollowGroup(long ownerUserId, long groupId) {
        String sql = "DELETE FROM follow_group WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, groupId, ownerUserId) > 0;
    }

    /**
     * 将某条关注关系归入分组或清空分组（groupId 为 null 表示未分组）
     */
    public boolean setFollowingGroup(long followerId, long followingId, Long groupId) {
        String exists = "SELECT COUNT(*) FROM fans WHERE follower_id = ? AND following_id = ?";
        Integer cnt = jdbcTemplate.queryForObject(exists, Integer.class, followerId, followingId);
        if (cnt == null || cnt == 0) {
            return false;
        }
        if (groupId != null) {
            String ok = "SELECT COUNT(*) FROM follow_group WHERE id = ? AND user_id = ?";
            Integer g = jdbcTemplate.queryForObject(ok, Integer.class, groupId, followerId);
            if (g == null || g == 0) {
                throw new IllegalArgumentException("分组不存在或不属于当前用户");
            }
        }
        String upd = "UPDATE fans SET group_id = ? WHERE follower_id = ? AND following_id = ?";
        int rows = jdbcTemplate.update(upd, groupId, followerId, followingId);
        return rows > 0;
    }
}

