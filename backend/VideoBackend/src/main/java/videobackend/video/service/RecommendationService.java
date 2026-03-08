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

    // 为了兼顾效果与性能，只选取最相似的前 K 个邻居参与打分
    private static final int TOP_K_NEIGHBORS = 50;

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

        // 只保留 Top-K 相似用户，并过滤掉相似度 <= 0 的用户
        List<Map.Entry<Long, Double>> topNeighbors = userSimilarities.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(TOP_K_NEIGHBORS)
                .collect(Collectors.toList());

        if (topNeighbors.isEmpty()) {
            // 没有任何有效邻居，回退到热门视频
            return videoService.listTopByViewCount(limit);
        }

        // 计算视频推荐分数（加权平均：sum(sim * score) / sum(sim)）
        Map<String, Double> scoreSums = new HashMap<>();
        Map<String, Double> weightSums = new HashMap<>();

        for (Map.Entry<Long, Double> entry : topNeighbors) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();

            Map<String, Double> similarUserRatings = userVideoMatrix.get(similarUserId);
            if (similarUserRatings == null || similarUserRatings.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Double> rating : similarUserRatings.entrySet()) {
                String videoId = rating.getKey();
                Double score = rating.getValue();

                // 跳过用户已观看的视频
                if (userRatings.containsKey(videoId)) continue;

                scoreSums.put(videoId,
                        scoreSums.getOrDefault(videoId, 0.0) + similarity * score);
                weightSums.put(videoId,
                        weightSums.getOrDefault(videoId, 0.0) + Math.abs(similarity));
            }
        }

        // 归一化得到最终打分
        Map<String, Double> videoScores = new HashMap<>();
        for (Map.Entry<String, Double> entry : scoreSums.entrySet()) {
            String videoId = entry.getKey();
            double sumSim = weightSums.getOrDefault(videoId, 0.0);
            if (sumSim > 0) {
                videoScores.put(videoId, entry.getValue() / sumSim);
            }
        }

        if (videoScores.isEmpty()) {
            // 理论上不太会发生，但为了稳妥再兜底一次
            return videoService.listTopByViewCount(limit);
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

        if (videoScores.isEmpty()) {
            // 没有找到任何相似视频，回退到热门
            return videoService.listTopByViewCount(limit);
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
     * 基于矩阵分解的推荐（简化版 MF：在内存中对当前行为矩阵做小规模训练）
     *
     * 说明：
     * - 仅用于教学 / Demo 场景，适合本项目这种中小规模数据。
     * - 对所有已有用户行为做一次小规模 SGD 训练，得到用户 / 视频的潜在因子向量。
     * - 在线请求时根据 userId 的向量与视频向量做内积打分。
     */
    public List<VideoItem> recommendByMatrixFactorization(Long userId, int limit) {
        Map<Long, Map<String, Double>> userVideoMatrix = behaviorRepository.getUserVideoMatrix();
        if (userVideoMatrix.isEmpty() || !userVideoMatrix.containsKey(userId)) {
            // 没有行为数据，直接回退热门
            return videoService.listTopByViewCount(limit);
        }

        // 为用户和视频建立索引映射
        List<Long> userIds = new ArrayList<>(userVideoMatrix.keySet());
        Map<Long, Integer> userIndex = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            userIndex.put(userIds.get(i), i);
        }

        // 收集所有视频 ID
        Set<String> videoIdSet = new HashSet<>();
        for (Map<String, Double> ratings : userVideoMatrix.values()) {
            videoIdSet.addAll(ratings.keySet());
        }
        List<String> videoIds = new ArrayList<>(videoIdSet);
        Map<String, Integer> videoIndex = new HashMap<>();
        for (int j = 0; j < videoIds.size(); j++) {
            videoIndex.put(videoIds.get(j), j);
        }

        int numUsers = userIds.size();
        int numItems = videoIds.size();
        if (numUsers == 0 || numItems == 0 || !userIndex.containsKey(userId)) {
            return videoService.listTopByViewCount(limit);
        }

        // 超参数：潜在因子维度 / 学习率 / 正则 / 迭代轮数
        int factors = 10;
        double learningRate = 0.01;
        double reg = 0.05;
        int epochs = 20;

        // 初始化潜在因子矩阵 P(user x k), Q(item x k)
        double[][] P = new double[numUsers][factors];
        double[][] Q = new double[numItems][factors];
        Random random = new Random(42);
        for (int u = 0; u < numUsers; u++) {
            for (int f = 0; f < factors; f++) {
                P[u][f] = (random.nextDouble() - 0.5) * 0.1;
            }
        }
        for (int i = 0; i < numItems; i++) {
            for (int f = 0; f < factors; f++) {
                Q[i][f] = (random.nextDouble() - 0.5) * 0.1;
            }
        }

        // 构造训练样本 (uIdx, iIdx, rating)
        List<int[]> samples = new ArrayList<>();
        List<Double> sampleRatings = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Double>> ue : userVideoMatrix.entrySet()) {
            int uIdx = userIndex.get(ue.getKey());
            for (Map.Entry<String, Double> re : ue.getValue().entrySet()) {
                Integer iIdx = videoIndex.get(re.getKey());
                if (iIdx == null) continue;
                samples.add(new int[]{uIdx, iIdx});
                sampleRatings.add(re.getValue());
            }
        }

        if (samples.isEmpty()) {
            return videoService.listTopByViewCount(limit);
        }

        // 简单 SGD 训练
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (int s = 0; s < samples.size(); s++) {
                int[] ui = samples.get(s);
                int u = ui[0];
                int i = ui[1];
                double rating = sampleRatings.get(s);

                // 预测值 = P[u] · Q[i]
                double pred = 0.0;
                for (int f = 0; f < factors; f++) {
                    pred += P[u][f] * Q[i][f];
                }
                double err = rating - pred;

                // 梯度更新
                for (int f = 0; f < factors; f++) {
                    double pu = P[u][f];
                    double qi = Q[i][f];
                    P[u][f] += learningRate * (err * qi - reg * pu);
                    Q[i][f] += learningRate * (err * pu - reg * qi);
                }
            }
        }

        // 对目标用户打分所有未看过的视频
        int targetIndex = userIndex.get(userId);
        Map<String, Double> targetRatings = userVideoMatrix.getOrDefault(userId, Collections.emptyMap());
        Map<String, Double> scores = new HashMap<>();

        for (int i = 0; i < numItems; i++) {
            String vid = videoIds.get(i);
            if (targetRatings.containsKey(vid)) continue; // 已有行为的视频跳过

            double score = 0.0;
            for (int f = 0; f < factors; f++) {
                score += P[targetIndex][f] * Q[i][f];
            }
            scores.put(vid, score);
        }

        if (scores.isEmpty()) {
            return videoService.listTopByViewCount(limit);
        }

        // 排序并返回 Top-N
        List<String> recommendedVideoIds = scores.entrySet().stream()
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



