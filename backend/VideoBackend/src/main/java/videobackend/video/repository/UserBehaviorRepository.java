package videobackend.video.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import videobackend.video.model.UserBehavior;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户行为数据仓库
 * 从数据库提取用户行为数据用于推荐系统
 */
@Repository
public class UserBehaviorRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserBehaviorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取所有用户行为数据（用于推荐算法训练）
     */
    public List<UserBehavior> getAllBehaviors() {
        String sql = """
            SELECT user_id, video_id, 'LIKE' as type, 1.0 as score, create_time as timestamp
            FROM video_likes
            UNION ALL
            SELECT user_id, video_id, 'FAVORITE' as type, 0.8 as score, create_time as timestamp
            FROM favorites
            UNION ALL
            SELECT user_id, video_id, 'WATCH' as type, 
                   CASE 
                       WHEN play_time * 1.0 / NULLIF(duration, 0) > 0.5 THEN 0.6
                       WHEN play_time * 1.0 / NULLIF(duration, 0) > 0.25 THEN 0.4
                       WHEN play_time > 0 THEN 0.2
                       ELSE 0.1
                   END as score,
                   create_time as timestamp
            FROM play_history
            ORDER BY timestamp DESC
            """;
        
        return jdbcTemplate.query(sql, this::mapToUserBehavior);
    }

    /**
     * 获取指定用户的行为数据
     */
    public List<UserBehavior> getUserBehaviors(Long userId) {
        String sql = """
            SELECT user_id, video_id, 'LIKE' as type, 1.0 as score, create_time as timestamp
            FROM video_likes
            WHERE user_id = ?
            UNION ALL
            SELECT user_id, video_id, 'FAVORITE' as type, 0.8 as score, create_time as timestamp
            FROM favorites
            WHERE user_id = ?
            UNION ALL
            SELECT user_id, video_id, 'WATCH' as type,
                   CASE 
                       WHEN play_time * 1.0 / NULLIF(duration, 0) > 0.5 THEN 0.6
                       WHEN play_time * 1.0 / NULLIF(duration, 0) > 0.25 THEN 0.4
                       WHEN play_time > 0 THEN 0.2
                       ELSE 0.1
                   END as score,
                   create_time as timestamp
            FROM play_history
            WHERE user_id = ?
            ORDER BY timestamp DESC
            """;
        
        return jdbcTemplate.query(sql, this::mapToUserBehavior, userId, userId, userId);
    }

    /**
     * 获取用户-视频评分矩阵（用于协同过滤）
     */
    public Map<Long, Map<String, Double>> getUserVideoMatrix() {
        List<UserBehavior> behaviors = getAllBehaviors();
        return behaviors.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UserBehavior::userId,
                        java.util.stream.Collectors.toMap(
                                UserBehavior::videoId,
                                UserBehavior::score,
                                Math::max  // 如果同一用户对同一视频有多个行为，取最高分
                        )
                ));
    }

    /**
     * 检查用户是否为可疑用户（用于防御）
     */
    public boolean isSuspiciousUser(Long userId) {
        String sql = """
            SELECT 
                COUNT(DISTINCT video_id) as video_count,
                COUNT(*) as total_actions,
                AVG(CASE WHEN type = 'LIKE' THEN 1 ELSE 0 END) as like_ratio
            FROM (
                SELECT video_id, 'LIKE' as type FROM video_likes WHERE user_id = ?
                UNION ALL
                SELECT video_id, 'FAVORITE' as type FROM favorites WHERE user_id = ?
            ) as user_actions
            """;
        
        // 这里可以添加更复杂的检测逻辑
        // 例如：行为数量异常、点赞比例异常等
        return false; // 简化实现
    }

    private UserBehavior mapToUserBehavior(ResultSet rs, int rowNum) throws SQLException {
        return new UserBehavior(
                rs.getLong("user_id"),
                rs.getString("video_id"),
                UserBehavior.BehaviorType.valueOf(rs.getString("type")),
                rs.getDouble("score"),
                rs.getTimestamp("timestamp") != null 
                    ? rs.getTimestamp("timestamp").toLocalDateTime() 
                    : LocalDateTime.now()
        );
    }
}



