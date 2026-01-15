package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import videobackend.video.model.UserBehavior;
import videobackend.video.model.VideoItem;
import videobackend.video.repository.UserBehaviorRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐系统服务
 * 实现基于用户行为的协同过滤推荐算法
 */
@Service
public class RecommendationService {

    private final JdbcTemplate jdbcTemplate;
    private final UserBehaviorRepository behaviorRepository;
    private final LocalVideoService videoService;

    public RecommendationService(JdbcTemplate jdbcTemplate,
                                UserBehaviorRepository behaviorRepository,
                                LocalVideoService videoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.behaviorRepository = behaviorRepository;
        this.videoService = videoService;
    }

    /**
     * 为用户推荐视频（基于用户的协同过滤）
     */
    public List<VideoItem> recommendForUser(Long userId, int limit) {
        // 获取用户行为矩阵
        Map<Long, Map<String, Double>> userVideoMatrix = behaviorRepository.getUserVideoMatrix();
        
        if (!userVideoMatrix.containsKey(userId) || userVideoMatrix.get(userId).isEmpty()) {
            // 新用户，返回热门视频
            return videoService.listTopByViewCount(limit);
        }

        // 计算用户相似度
        Map<String, Double> userRatings = userVideoMatrix.get(userId);
        Map<Long, Double> userSimilarities = calculateUserSimilarities(userId, userRatings, userVideoMatrix);

        // 计算视频推荐分数
        Map<String, Double> videoScores = new HashMap<>();
        
        for (Map.Entry<Long, Double> entry : userSimilarities.entrySet()) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();
            
            if (similarity <= 0) continue;
            
            Map<String, Double> similarUserRatings = userVideoMatrix.get(similarUserId);
            for (Map.Entry<String, Double> rating : similarUserRatings.entrySet()) {
                String videoId = rating.getKey();
                Double score = rating.getValue();
                
                // 跳过用户已观看的视频
                if (userRatings.containsKey(videoId)) continue;
                
                videoScores.put(videoId, 
                    videoScores.getOrDefault(videoId, 0.0) + similarity * score);
            }
        }

        // 按分数排序并返回Top-K
        List<String> recommendedVideoIds = videoScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 获取视频详情
        return recommendedVideoIds.stream()
                .map(videoId -> videoService.findByVideoId(videoId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * 计算用户相似度（余弦相似度）
     */
    private Map<Long, Double> calculateUserSimilarities(Long targetUserId, 
                                                         Map<String, Double> targetRatings,
                                                         Map<Long, Map<String, Double>> userVideoMatrix) {
        Map<Long, Double> similarities = new HashMap<>();
        
        for (Map.Entry<Long, Map<String, Double>> entry : userVideoMatrix.entrySet()) {
            Long userId = entry.getKey();
            if (userId.equals(targetUserId)) continue;
            
            Map<String, Double> userRatings = entry.getValue();
            double similarity = cosineSimilarity(targetRatings, userRatings);
            similarities.put(userId, similarity);
        }
        
        return similarities;
    }

    /**
     * 计算两个用户评分向量的余弦相似度
     */
    private double cosineSimilarity(Map<String, Double> ratings1, Map<String, Double> ratings2) {
        Set<String> commonVideos = new HashSet<>(ratings1.keySet());
        commonVideos.retainAll(ratings2.keySet());
        
        if (commonVideos.isEmpty()) return 0.0;
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String videoId : commonVideos) {
            double r1 = ratings1.get(videoId);
            double r2 = ratings2.get(videoId);
            dotProduct += r1 * r2;
            norm1 += r1 * r1;
            norm2 += r2 * r2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 基于物品的协同过滤推荐
     */
    public List<VideoItem> recommendByItem(Long userId, int limit) {
        // 获取用户已观看的视频
        List<UserBehavior> userBehaviors = behaviorRepository.getUserBehaviors(userId);
        Set<String> watchedVideos = userBehaviors.stream()
                .map(UserBehavior::videoId)
                .collect(Collectors.toSet());
        
        if (watchedVideos.isEmpty()) {
            return videoService.listTopByViewCount(limit);
        }

        // 计算视频相似度并推荐
        Map<String, Double> videoScores = new HashMap<>();
        
        for (String watchedVideo : watchedVideos) {
            // 找到与已观看视频相似的其他视频
            List<String> similarVideos = findSimilarVideos(watchedVideo, watchedVideos);
            
            for (String similarVideo : similarVideos) {
                double similarity = calculateVideoSimilarity(watchedVideo, similarVideo);
                videoScores.put(similarVideo, 
                    videoScores.getOrDefault(similarVideo, 0.0) + similarity);
            }
        }

        // 按分数排序
        List<String> recommendedVideoIds = videoScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return recommendedVideoIds.stream()
                .map(videoId -> videoService.findByVideoId(videoId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * 找到与指定视频相似的其他视频
     */
    private List<String> findSimilarVideos(String videoId, Set<String> excludeVideos) {
        String sql = """
            SELECT DISTINCT v2.video_id
            FROM (
                SELECT user_id FROM video_likes WHERE video_id = ?
                UNION
                SELECT user_id FROM favorites WHERE video_id = ?
                UNION
                SELECT user_id FROM play_history WHERE video_id = ? AND play_time > 0
            ) as users
            JOIN video_likes v2 ON users.user_id = v2.user_id AND v2.video_id != ?
            UNION
            SELECT DISTINCT v2.video_id
            FROM (
                SELECT user_id FROM video_likes WHERE video_id = ?
                UNION
                SELECT user_id FROM favorites WHERE video_id = ?
            ) as users
            JOIN favorites v2 ON users.user_id = v2.user_id AND v2.video_id != ?
            LIMIT 50
            """;
        
        List<String> similarVideos = jdbcTemplate.query(sql, 
            (rs, rowNum) -> rs.getString("video_id"),
            videoId, videoId, videoId, videoId, videoId, videoId, videoId);
        
        return similarVideos.stream()
                .filter(v -> !excludeVideos.contains(v))
                .collect(Collectors.toList());
    }

    /**
     * 计算两个视频的相似度
     */
    private double calculateVideoSimilarity(String videoId1, String videoId2) {
        String sql = """
            SELECT COUNT(DISTINCT user_id) as common_users
            FROM (
                SELECT user_id FROM video_likes WHERE video_id = ?
                UNION
                SELECT user_id FROM favorites WHERE video_id = ?
            ) as v1_users
            WHERE user_id IN (
                SELECT user_id FROM video_likes WHERE video_id = ?
                UNION
                SELECT user_id FROM favorites WHERE video_id = ?
            )
            """;
        
        Integer commonUsers = jdbcTemplate.queryForObject(sql, Integer.class,
            videoId1, videoId1, videoId2, videoId2);
        
        if (commonUsers == null || commonUsers == 0) return 0.0;
        
        // 简化的相似度计算（Jaccard相似度）
        String sql1 = """
            SELECT COUNT(DISTINCT user_id) as total_users
            FROM (
                SELECT user_id FROM video_likes WHERE video_id IN (?, ?)
                UNION
                SELECT user_id FROM favorites WHERE video_id IN (?, ?)
            ) as all_users
            """;
        
        Integer totalUsers = jdbcTemplate.queryForObject(sql1, Integer.class,
            videoId1, videoId2, videoId1, videoId2);
        
        if (totalUsers == null || totalUsers == 0) return 0.0;
        
        return commonUsers * 1.0 / totalUsers;
    }
}



