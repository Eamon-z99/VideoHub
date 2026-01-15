package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import videobackend.video.repository.UserBehaviorRepository;

import java.util.*;

/**
 * 防御服务
 * 检测和防御投毒攻击
 */
@Service
public class DefenseService {

    private final JdbcTemplate jdbcTemplate;
    private final UserBehaviorRepository behaviorRepository;

    // 防御阈值配置
    private static final double SUSPICIOUS_LIKE_RATIO = 0.8;  // 点赞比例超过80%视为可疑
    private static final int MIN_NORMAL_ACTIONS = 10;  // 正常用户最少行为数
    private static final int MAX_ACTIONS_PER_DAY = 100;  // 单日最大行为数
    private static final double MIN_ENTROPY = 0.5;  // 最小行为熵（多样性）

    public DefenseService(JdbcTemplate jdbcTemplate, UserBehaviorRepository behaviorRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.behaviorRepository = behaviorRepository;
    }

    /**
     * 检测用户是否为攻击者
     */
    public DefenseResult detectAttack(Long userId) {
        DefenseResult result = new DefenseResult(userId);
        
        // 1. 检查行为数量
        int actionCount = getActionCount(userId);
        result.setActionCount(actionCount);
        if (actionCount < MIN_NORMAL_ACTIONS) {
            result.addSuspicion("行为数量过少: " + actionCount);
        }
        
        // 2. 检查点赞比例
        double likeRatio = getLikeRatio(userId);
        result.setLikeRatio(likeRatio);
        if (likeRatio > SUSPICIOUS_LIKE_RATIO) {
            result.addSuspicion("点赞比例异常: " + String.format("%.2f%%", likeRatio * 100));
        }
        
        // 3. 检查行为熵（多样性）
        double entropy = calculateEntropy(userId);
        result.setEntropy(entropy);
        if (entropy < MIN_ENTROPY) {
            result.addSuspicion("行为多样性不足，熵值: " + String.format("%.2f", entropy));
        }
        
        // 4. 检查时间分布
        boolean timeAnomaly = checkTimeDistribution(userId);
        result.setTimeAnomaly(timeAnomaly);
        if (timeAnomaly) {
            result.addSuspicion("时间分布异常");
        }
        
        // 5. 检查目标视频集中度
        double targetConcentration = checkTargetConcentration(userId);
        result.setTargetConcentration(targetConcentration);
        if (targetConcentration > 0.5) {
            result.addSuspicion("目标视频集中度过高: " + String.format("%.2f%%", targetConcentration * 100));
        }
        
        // 综合评分
        result.calculateRiskScore();
        
        return result;
    }

    /**
     * 获取用户行为数量
     */
    private int getActionCount(Long userId) {
        String sql = """
            SELECT COUNT(*) as cnt FROM (
                SELECT video_id FROM video_likes WHERE user_id = ?
                UNION ALL
                SELECT video_id FROM favorites WHERE user_id = ?
                UNION ALL
                SELECT video_id FROM play_history WHERE user_id = ? AND play_time > 0
            ) as actions
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, userId, userId);
        return count != null ? count : 0;
    }

    /**
     * 计算点赞比例
     */
    private double getLikeRatio(Long userId) {
        String sql = """
            SELECT 
                COUNT(CASE WHEN type = 'like' THEN 1 END) * 1.0 / COUNT(*) as ratio
            FROM (
                SELECT 'like' as type FROM video_likes WHERE user_id = ?
                UNION ALL
                SELECT 'favorite' as type FROM favorites WHERE user_id = ?
                UNION ALL
                SELECT 'watch' as type FROM play_history WHERE user_id = ? AND play_time > 0
            ) as actions
            """;
        Double ratio = jdbcTemplate.queryForObject(sql, Double.class, userId, userId, userId);
        return ratio != null ? ratio : 0.0;
    }

    /**
     * 计算行为熵（多样性）
     */
    private double calculateEntropy(Long userId) {
        String sql = """
            SELECT video_id, COUNT(*) as cnt
            FROM (
                SELECT video_id FROM video_likes WHERE user_id = ?
                UNION ALL
                SELECT video_id FROM favorites WHERE user_id = ?
                UNION ALL
                SELECT video_id FROM play_history WHERE user_id = ? AND play_time > 0
            ) as actions
            GROUP BY video_id
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId, userId, userId);
        
        if (results.isEmpty()) return 0.0;
        
        int total = results.stream()
                .mapToInt(r -> ((Number) r.get("cnt")).intValue())
                .sum();
        
        double entropy = 0.0;
        for (Map<String, Object> row : results) {
            int count = ((Number) row.get("cnt")).intValue();
            double p = count * 1.0 / total;
            if (p > 0) {
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }
        
        return entropy;
    }

    /**
     * 检查时间分布
     */
    private boolean checkTimeDistribution(Long userId) {
        String sql = """
            SELECT DATE(create_time) as date, COUNT(*) as cnt
            FROM (
                SELECT create_time FROM video_likes WHERE user_id = ?
                UNION ALL
                SELECT create_time FROM favorites WHERE user_id = ?
            ) as actions
            GROUP BY DATE(create_time)
            HAVING cnt > ?
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, 
            userId, userId, MAX_ACTIONS_PER_DAY);
        
        return !results.isEmpty();
    }

    /**
     * 检查目标视频集中度
     */
    private double checkTargetConcentration(Long userId) {
        // 找到用户最常互动的视频
        String sql = """
            SELECT video_id, COUNT(*) as cnt
            FROM (
                SELECT video_id FROM video_likes WHERE user_id = ?
                UNION ALL
                SELECT video_id FROM favorites WHERE user_id = ?
            ) as actions
            GROUP BY video_id
            ORDER BY cnt DESC
            LIMIT 5
            """;
        
        List<Map<String, Object>> topVideos = jdbcTemplate.queryForList(sql, userId, userId);
        
        if (topVideos.isEmpty()) return 0.0;
        
        int totalActions = getActionCount(userId);
        if (totalActions == 0) return 0.0;
        
        int topActions = topVideos.stream()
                .mapToInt(r -> ((Number) r.get("cnt")).intValue())
                .sum();
        
        return topActions * 1.0 / totalActions;
    }

    /**
     * 过滤可疑用户的行为
     */
    public Set<Long> getSuspiciousUsers() {
        String sql = """
            SELECT DISTINCT user_id
            FROM (
                SELECT user_id FROM video_likes
                UNION
                SELECT user_id FROM favorites
            ) as users
            """;
        
        List<Long> allUsers = jdbcTemplate.queryForList(sql, Long.class);
        Set<Long> suspiciousUsers = new HashSet<>();
        
        for (Long userId : allUsers) {
            DefenseResult result = detectAttack(userId);
            if (result.getRiskScore() > 0.6) {  // 风险分数超过0.6视为可疑
                suspiciousUsers.add(userId);
            }
        }
        
        return suspiciousUsers;
    }

    /**
     * 防御结果类
     */
    public static class DefenseResult {
        private Long userId;
        private int actionCount;
        private double likeRatio;
        private double entropy;
        private boolean timeAnomaly;
        private double targetConcentration;
        private double riskScore;
        private List<String> suspicions = new ArrayList<>();
        private boolean isSuspicious = false;

        public DefenseResult(Long userId) {
            this.userId = userId;
        }

        public void calculateRiskScore() {
            double score = 0.0;
            
            if (actionCount < MIN_NORMAL_ACTIONS) score += 0.2;
            if (likeRatio > SUSPICIOUS_LIKE_RATIO) score += 0.3;
            if (entropy < MIN_ENTROPY) score += 0.2;
            if (timeAnomaly) score += 0.15;
            if (targetConcentration > 0.5) score += 0.15;
            
            this.riskScore = Math.min(1.0, score);
            this.isSuspicious = riskScore > 0.6;
        }

        public void addSuspicion(String reason) {
            suspicions.add(reason);
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public int getActionCount() { return actionCount; }
        public void setActionCount(int actionCount) { this.actionCount = actionCount; }
        public double getLikeRatio() { return likeRatio; }
        public void setLikeRatio(double likeRatio) { this.likeRatio = likeRatio; }
        public double getEntropy() { return entropy; }
        public void setEntropy(double entropy) { this.entropy = entropy; }
        public boolean isTimeAnomaly() { return timeAnomaly; }
        public void setTimeAnomaly(boolean timeAnomaly) { this.timeAnomaly = timeAnomaly; }
        public double getTargetConcentration() { return targetConcentration; }
        public void setTargetConcentration(double targetConcentration) { this.targetConcentration = targetConcentration; }
        public double getRiskScore() { return riskScore; }
        public List<String> getSuspicions() { return suspicions; }
        public boolean isSuspicious() { return isSuspicious; }
    }
}



