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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 推荐系统服务
 * 实现基于用户行为的协同过滤推荐算法
 */
@Service
public class RecommendationService {

    // 为了兼顾效果与性能，只选取最相似的前 K 个邻居参与打分
    private static final int TOP_K_NEIGHBORS = 50;
    // 每个请求的推荐计算时间预算（超过则快速回退热门，避免接口超时）
    private static final long TIME_BUDGET_MS = 1500;
    // 行为矩阵缓存 TTL（避免每次请求都全表扫描 UNION）
    private static final long MATRIX_CACHE_TTL_MS = 2 * 60 * 1000;
    // MF 模型缓存 TTL（建议更长；这里先用 10 分钟）
    private static final long MF_MODEL_TTL_MS = 10 * 60 * 1000;
    // Item-CF 只取用户最近 N 个交互视频参与计算，避免 watchedVideos 过大
    private static final int ITEM_CF_MAX_SEED_VIDEOS = 20;

    private final JdbcTemplate jdbcTemplate;
    private final UserBehaviorRepository behaviorRepository;
    private final LocalVideoService videoService;

    /**
     * 轻量内存缓存：缓存行为矩阵 + 倒排索引（video -> users），避免每次请求全量构建。
     */
    private volatile CachedBehaviorData behaviorCache;
    private final Object behaviorCacheLock = new Object();

    /**
     * MF 模型缓存：在线只做打分，不做训练；训练放到后台线程。
     */
    private volatile CachedMfModel mfCache;
    private final Object mfCacheLock = new Object();
    private volatile CompletableFuture<CachedMfModel> mfTrainingFuture;
    private final ExecutorService mfExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mf-trainer");
        t.setDaemon(true);
        return t;
    });

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
        long start = System.currentTimeMillis();
        // 获取用户行为矩阵
        CachedBehaviorData cached = getCachedBehaviorData();
        Map<Long, Map<String, Double>> userVideoMatrix = cached.userVideoMatrix;
        
        if (!userVideoMatrix.containsKey(userId) || userVideoMatrix.get(userId).isEmpty()) {
            // 新用户，返回热门视频
            return videoService.listTopByViewCount(limit);
        }

        // 计算用户相似度
        Map<String, Double> userRatings = userVideoMatrix.get(userId);
        if (isOverBudget(start)) {
            return videoService.listTopByViewCount(limit);
        }

        // 只对“与目标用户有交集行为”的候选用户计算相似度，避免遍历所有用户
        Set<Long> candidateUsers = new HashSet<>();
        for (String vid : userRatings.keySet()) {
            List<Long> users = cached.videoToUsers.get(vid);
            if (users != null) candidateUsers.addAll(users);
        }
        candidateUsers.remove(userId);

        Map<Long, Double> userSimilarities = calculateUserSimilarities(userId, userRatings, userVideoMatrix, candidateUsers);

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
            if (isOverBudget(start)) {
                return videoService.listTopByViewCount(limit);
            }
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
        List<VideoItem> result = recommendedVideoIds.stream()
                .map(videoId -> videoService.findByVideoId(videoId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return result.isEmpty() ? videoService.listTopByViewCount(limit) : result;
    }

    /**
     * 为离线实验准备的完整版 User-CF：不做时间预算限制，遍历所有用户计算相似度。
     */
    public List<VideoItem> recommendForUserFull(Long userId, int limit) {
        Map<Long, Map<String, Double>> userVideoMatrix = behaviorRepository.getUserVideoMatrix();
        if (!userVideoMatrix.containsKey(userId) || userVideoMatrix.get(userId).isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> userRatings = userVideoMatrix.get(userId);
        // 候选用户 = 除自己外的所有用户
        Set<Long> candidates = new HashSet<>(userVideoMatrix.keySet());
        candidates.remove(userId);
        Map<Long, Double> userSimilarities = calculateUserSimilarities(userId, userRatings, userVideoMatrix, candidates);

        List<Map.Entry<Long, Double>> topNeighbors = userSimilarities.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(TOP_K_NEIGHBORS)
                .collect(Collectors.toList());

        if (topNeighbors.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> scoreSums = new HashMap<>();
        Map<String, Double> weightSums = new HashMap<>();
        for (Map.Entry<Long, Double> entry : topNeighbors) {
            Long similarUserId = entry.getKey();
            Double similarity = entry.getValue();
            Map<String, Double> similarUserRatings = userVideoMatrix.get(similarUserId);
            if (similarUserRatings == null || similarUserRatings.isEmpty()) continue;

            for (Map.Entry<String, Double> rating : similarUserRatings.entrySet()) {
                String vid = rating.getKey();
                Double score = rating.getValue();
                if (userRatings.containsKey(vid)) continue;
                scoreSums.put(vid, scoreSums.getOrDefault(vid, 0.0) + similarity * score);
                weightSums.put(vid, weightSums.getOrDefault(vid, 0.0) + Math.abs(similarity));
            }
        }

        Map<String, Double> videoScores = new HashMap<>();
        for (Map.Entry<String, Double> e : scoreSums.entrySet()) {
            double sumSim = weightSums.getOrDefault(e.getKey(), 0.0);
            if (sumSim > 0) {
                videoScores.put(e.getKey(), e.getValue() / sumSim);
            }
        }
        if (videoScores.isEmpty()) {
            return Collections.emptyList();
        }

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
     * 计算用户相似度（余弦相似度）
     */
    private Map<Long, Double> calculateUserSimilarities(Long targetUserId,
                                                         Map<String, Double> targetRatings,
                                                         Map<Long, Map<String, Double>> userVideoMatrix,
                                                         Set<Long> candidateUsers) {
        Map<Long, Double> similarities = new HashMap<>();

        if (candidateUsers == null || candidateUsers.isEmpty()) {
            return similarities;
        }

        for (Long userId : candidateUsers) {
            if (userId == null || userId.equals(targetUserId)) continue;
            Map<String, Double> userRatings = userVideoMatrix.get(userId);
            if (userRatings == null || userRatings.isEmpty()) continue;
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
        long start = System.currentTimeMillis();
        // 获取用户已观看的视频
        List<UserBehavior> userBehaviors = behaviorRepository.getUserBehaviors(userId);
        // 只取最近 N 个种子视频，避免 seeds 过多导致 SQL 次数爆炸
        Set<String> watchedVideos = userBehaviors.stream()
                .map(UserBehavior::videoId)
                .filter(Objects::nonNull)
                .limit(ITEM_CF_MAX_SEED_VIDEOS)
                .collect(Collectors.toSet());
        
        if (watchedVideos.isEmpty()) {
            return videoService.listTopByViewCount(limit);
        }

        // 计算视频相似度并推荐
        Map<String, Double> videoScores = new HashMap<>();
        
        for (String watchedVideo : watchedVideos) {
            if (isOverBudget(start)) {
                return videoService.listTopByViewCount(limit);
            }
            // 聚合 SQL：一次查出相似候选及“共现用户数”得分，避免每个 pair 再查相似度
            Map<String, Double> similarWithScore = findSimilarVideosWithScore(watchedVideo, watchedVideos);
            for (Map.Entry<String, Double> e : similarWithScore.entrySet()) {
                String similarVideo = e.getKey();
                double score = e.getValue() == null ? 0.0 : e.getValue();
                if (score <= 0) continue;
                videoScores.put(similarVideo, videoScores.getOrDefault(similarVideo, 0.0) + score);
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
     * 为离线实验准备的完整版 Item-CF：
     * - 不限制种子视频数量；
     * - 对候选视频再计算 Jaccard 相似度。
     */
    public List<VideoItem> recommendByItemFull(Long userId, int limit) {
        List<UserBehavior> userBehaviors = behaviorRepository.getUserBehaviors(userId);
        Set<String> watchedVideos = userBehaviors.stream()
                .map(UserBehavior::videoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (watchedVideos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> videoScores = new HashMap<>();
        for (String watchedVideo : watchedVideos) {
            List<String> candidates = findSimilarVideosDetailed(watchedVideo, watchedVideos);
            for (String cand : candidates) {
                double sim = calculateVideoSimilarity(watchedVideo, cand);
                if (sim <= 0) continue;
                videoScores.put(cand, videoScores.getOrDefault(cand, 0.0) + sim);
            }
        }

        if (videoScores.isEmpty()) {
            return Collections.emptyList();
        }

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
        long start = System.currentTimeMillis();
        CachedBehaviorData behavior = getCachedBehaviorData();
        Map<Long, Map<String, Double>> userVideoMatrix = behavior.userVideoMatrix;

        if (userVideoMatrix.isEmpty() || !userVideoMatrix.containsKey(userId) || userVideoMatrix.get(userId).isEmpty()) {
            return videoService.listTopByViewCount(limit);
        }

        // 1) 尝试直接用缓存模型快速打分
        CachedMfModel model = getCachedMfModelIfFresh();
        if (model != null && model.userIndex.containsKey(userId)) {
            return scoreByMfModel(userId, limit, userVideoMatrix, model, start);
        }

        // 2) 模型未就绪：触发后台训练（不阻塞请求），本次请求直接回退热门，避免超时
        triggerMfTrainingIfNeeded(userVideoMatrix);
        return videoService.listTopByViewCount(limit);
    }

    /**
     * 为离线实验准备的完整版 MF：
     * - 每次按当前行为矩阵完整训练一次 MF 模型；
     * - 不做时间预算限制；
     * - 支持指定训练轮数。
     */
    public List<VideoItem> recommendByMatrixFactorizationFull(Long userId, int limit, int epochs) {
        Map<Long, Map<String, Double>> userVideoMatrix = behaviorRepository.getUserVideoMatrix();
        if (userVideoMatrix.isEmpty() || !userVideoMatrix.containsKey(userId) || userVideoMatrix.get(userId).isEmpty()) {
            return Collections.emptyList();
        }

        CachedMfModel fullModel = trainMfModelFull(userVideoMatrix, epochs);
        if (fullModel == null || !fullModel.userIndex.containsKey(userId)) {
            return Collections.emptyList();
        }
        return scoreByMfModelNoBudget(userId, limit, userVideoMatrix, fullModel);
    }

    /**
     * 在线 Item-CF：通过聚合 SQL 找到与指定视频相似的其他视频及粗略得分
     */
    private Map<String, Double> findSimilarVideosWithScore(String videoId, Set<String> excludeVideos) {
        // 共同用户数作为“相似度”得分（越大越相似）；一次聚合 SQL 避免 N+1 查询
        String sql = """
            SELECT cand.video_id AS video_id, COUNT(DISTINCT cand.user_id) AS common_users
            FROM (
                SELECT user_id FROM video_likes WHERE video_id = ?
                UNION
                SELECT user_id FROM favorites WHERE video_id = ?
                UNION
                SELECT user_id FROM play_history WHERE video_id = ? AND play_time > 0
            ) u
            JOIN (
                SELECT user_id, video_id FROM video_likes
                UNION ALL
                SELECT user_id, video_id FROM favorites
                UNION ALL
                SELECT user_id, video_id FROM play_history WHERE play_time > 0
            ) cand ON cand.user_id = u.user_id
            WHERE cand.video_id <> ?
            GROUP BY cand.video_id
            ORDER BY common_users DESC
            LIMIT 200
            """;

        List<Map.Entry<String, Double>> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> Map.entry(rs.getString("video_id"), rs.getDouble("common_users")),
                videoId, videoId, videoId, videoId);

        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> e : rows) {
            if (e.getKey() == null) continue;
            if (excludeVideos != null && excludeVideos.contains(e.getKey())) continue;
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }

    /**
     * 为离线实验准备：先按“共同用户”召回候选，再用更精细 SQL 做 Jaccard 相似度。
     */
    private List<String> findSimilarVideosDetailed(String videoId, Set<String> excludeVideos) {
        String sql = """
            SELECT DISTINCT v2.video_id
            FROM (
                SELECT user_id FROM video_likes WHERE video_id = ?
                UNION
                SELECT user_id FROM favorites WHERE video_id = ?
                UNION
                SELECT user_id FROM play_history WHERE video_id = ? AND play_time > 0
            ) AS users
            JOIN video_likes v2 ON users.user_id = v2.user_id AND v2.video_id <> ?
            UNION
            SELECT DISTINCT v2.video_id
            FROM (
                SELECT user_id FROM video_likes WHERE video_id = ?
                UNION
                SELECT user_id FROM favorites WHERE video_id = ?
            ) AS users
            JOIN favorites v2 ON users.user_id = v2.user_id AND v2.video_id <> ?
            LIMIT 200
            """;

        List<String> similarVideos = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getString("video_id"),
                videoId, videoId, videoId, videoId,
                videoId, videoId, videoId);

        return similarVideos.stream()
                .filter(v -> v != null && (excludeVideos == null || !excludeVideos.contains(v)))
                .collect(Collectors.toList());
    }

    /**
     * 计算两个视频的相似度（用于 Item-CF 完整实验）
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

    private boolean isOverBudget(long startMs) {
        return System.currentTimeMillis() - startMs > TIME_BUDGET_MS;
    }

    private CachedBehaviorData getCachedBehaviorData() {
        CachedBehaviorData cached = behaviorCache;
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.builtAtMs <= MATRIX_CACHE_TTL_MS) {
            return cached;
        }
        synchronized (behaviorCacheLock) {
            cached = behaviorCache;
            if (cached != null && now - cached.builtAtMs <= MATRIX_CACHE_TTL_MS) {
                return cached;
            }
            Map<Long, Map<String, Double>> matrix = behaviorRepository.getUserVideoMatrix();
            Map<String, List<Long>> videoToUsers = buildVideoToUsersIndex(matrix);
            CachedBehaviorData refreshed = new CachedBehaviorData(matrix, videoToUsers, now);
            behaviorCache = refreshed;
            return refreshed;
        }
    }

    private Map<String, List<Long>> buildVideoToUsersIndex(Map<Long, Map<String, Double>> matrix) {
        Map<String, List<Long>> index = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> ue : matrix.entrySet()) {
            Long uid = ue.getKey();
            Map<String, Double> ratings = ue.getValue();
            if (uid == null || ratings == null) continue;
            for (String vid : ratings.keySet()) {
                if (vid == null) continue;
                index.computeIfAbsent(vid, k -> new ArrayList<>()).add(uid);
            }
        }
        return index;
    }

    private CachedMfModel getCachedMfModelIfFresh() {
        CachedMfModel cached = mfCache;
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.builtAtMs <= MF_MODEL_TTL_MS) {
            return cached;
        }
        return null;
    }

    private void triggerMfTrainingIfNeeded(Map<Long, Map<String, Double>> userVideoMatrix) {
        long now = System.currentTimeMillis();
        CachedMfModel cached = mfCache;
        if (cached != null && now - cached.builtAtMs <= MF_MODEL_TTL_MS) {
            return;
        }
        synchronized (mfCacheLock) {
            cached = mfCache;
            if (cached != null && now - cached.builtAtMs <= MF_MODEL_TTL_MS) {
                return;
            }
            if (mfTrainingFuture != null && !mfTrainingFuture.isDone()) {
                return;
            }
            // 后台训练（不阻塞请求）
            mfTrainingFuture = CompletableFuture.supplyAsync(() -> trainMfModel(userVideoMatrix), mfExecutor)
                    .whenComplete((model, ex) -> {
                        if (ex == null && model != null) {
                            mfCache = model;
                        }
                    });
        }
    }

    private List<VideoItem> scoreByMfModel(Long userId,
                                          int limit,
                                          Map<Long, Map<String, Double>> userVideoMatrix,
                                          CachedMfModel model,
                                          long start) {
        Integer uIdxObj = model.userIndex.get(userId);
        if (uIdxObj == null) {
            return videoService.listTopByViewCount(limit);
        }
        int uIdx = uIdxObj;
        Map<String, Double> seen = userVideoMatrix.getOrDefault(userId, Collections.emptyMap());

        // 只打分未看过的视频
        Map<String, Double> scores = new HashMap<>();
        for (int i = 0; i < model.videoIds.size(); i++) {
            if (isOverBudget(start)) {
                return videoService.listTopByViewCount(limit);
            }
            String vid = model.videoIds.get(i);
            if (vid == null || seen.containsKey(vid)) continue;
            double score = 0.0;
            for (int f = 0; f < model.factors; f++) {
                score += model.P[uIdx][f] * model.Q[i][f];
            }
            scores.put(vid, score);
        }

        if (scores.isEmpty()) {
            return videoService.listTopByViewCount(limit);
        }

        List<String> recommendedVideoIds = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<VideoItem> result = recommendedVideoIds.stream()
                .map(videoId -> videoService.findByVideoId(videoId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return result.isEmpty() ? videoService.listTopByViewCount(limit) : result;
    }

    private CachedMfModel trainMfModel(Map<Long, Map<String, Double>> userVideoMatrix) {
        // 注意：训练可能耗时，放在后台；这里仍然是“简化版 MF”，但不会卡住接口请求
        if (userVideoMatrix == null || userVideoMatrix.isEmpty()) {
            return null;
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
            if (ratings != null) videoIdSet.addAll(ratings.keySet());
        }
        List<String> videoIds = new ArrayList<>(videoIdSet);
        Map<String, Integer> videoIndex = new HashMap<>();
        for (int j = 0; j < videoIds.size(); j++) {
            videoIndex.put(videoIds.get(j), j);
        }

        int numUsers = userIds.size();
        int numItems = videoIds.size();
        if (numUsers == 0 || numItems == 0) {
            return null;
        }

        // 超参数：潜在因子维度 / 学习率 / 正则 / 迭代轮数
        int factors = 10;
        double learningRate = 0.01;
        double reg = 0.05;
        int epochs = 10; // 后台训练也先降一档，避免初次训练过久

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
            Integer uIdx = userIndex.get(ue.getKey());
            if (uIdx == null) continue;
            Map<String, Double> ratings = ue.getValue();
            if (ratings == null) continue;
            for (Map.Entry<String, Double> re : ratings.entrySet()) {
                Integer iIdx = videoIndex.get(re.getKey());
                if (iIdx == null) continue;
                samples.add(new int[]{uIdx, iIdx});
                sampleRatings.add(re.getValue());
            }
        }

        if (samples.isEmpty()) {
            return null;
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

        return new CachedMfModel(P, Q, userIndex, videoIds, factors, System.currentTimeMillis());
    }

    /**
     * 为离线实验准备的 MF 训练版本：允许自定义轮数。
     */
    private CachedMfModel trainMfModelFull(Map<Long, Map<String, Double>> userVideoMatrix, int epochs) {
        if (userVideoMatrix == null || userVideoMatrix.isEmpty()) {
            return null;
        }

        List<Long> userIds = new ArrayList<>(userVideoMatrix.keySet());
        Map<Long, Integer> userIndex = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            userIndex.put(userIds.get(i), i);
        }

        Set<String> videoIdSet = new HashSet<>();
        for (Map<String, Double> ratings : userVideoMatrix.values()) {
            if (ratings != null) videoIdSet.addAll(ratings.keySet());
        }
        List<String> videoIds = new ArrayList<>(videoIdSet);
        Map<String, Integer> videoIndex = new HashMap<>();
        for (int j = 0; j < videoIds.size(); j++) {
            videoIndex.put(videoIds.get(j), j);
        }

        int numUsers = userIds.size();
        int numItems = videoIds.size();
        if (numUsers == 0 || numItems == 0) {
            return null;
        }

        int factors = 10;
        double learningRate = 0.01;
        double reg = 0.05;

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

        List<int[]> samples = new ArrayList<>();
        List<Double> sampleRatings = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Double>> ue : userVideoMatrix.entrySet()) {
            Integer uIdx = userIndex.get(ue.getKey());
            if (uIdx == null) continue;
            Map<String, Double> ratings = ue.getValue();
            if (ratings == null) continue;
            for (Map.Entry<String, Double> re : ratings.entrySet()) {
                Integer iIdx = videoIndex.get(re.getKey());
                if (iIdx == null) continue;
                samples.add(new int[]{uIdx, iIdx});
                sampleRatings.add(re.getValue());
            }
        }

        if (samples.isEmpty()) {
            return null;
        }

        for (int epoch = 0; epoch < epochs; epoch++) {
            for (int s = 0; s < samples.size(); s++) {
                int[] ui = samples.get(s);
                int u = ui[0];
                int i = ui[1];
                double rating = sampleRatings.get(s);

                double pred = 0.0;
                for (int f = 0; f < factors; f++) {
                    pred += P[u][f] * Q[i][f];
                }
                double err = rating - pred;

                for (int f = 0; f < factors; f++) {
                    double pu = P[u][f];
                    double qi = Q[i][f];
                    P[u][f] += learningRate * (err * qi - reg * pu);
                    Q[i][f] += learningRate * (err * pu - reg * qi);
                }
            }
        }

        return new CachedMfModel(P, Q, userIndex, videoIds, factors, System.currentTimeMillis());
    }

    private List<VideoItem> scoreByMfModelNoBudget(Long userId,
                                                   int limit,
                                                   Map<Long, Map<String, Double>> userVideoMatrix,
                                                   CachedMfModel model) {
        Integer uIdxObj = model.userIndex.get(userId);
        if (uIdxObj == null) {
            return Collections.emptyList();
        }
        int uIdx = uIdxObj;
        Map<String, Double> seen = userVideoMatrix.getOrDefault(userId, Collections.emptyMap());

        Map<String, Double> scores = new HashMap<>();
        for (int i = 0; i < model.videoIds.size(); i++) {
            String vid = model.videoIds.get(i);
            if (vid == null || seen.containsKey(vid)) continue;
            double score = 0.0;
            for (int f = 0; f < model.factors; f++) {
                score += model.P[uIdx][f] * model.Q[i][f];
            }
            scores.put(vid, score);
        }

        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

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

    private static class CachedBehaviorData {
        final Map<Long, Map<String, Double>> userVideoMatrix;
        final Map<String, List<Long>> videoToUsers;
        final long builtAtMs;

        private CachedBehaviorData(Map<Long, Map<String, Double>> userVideoMatrix,
                                   Map<String, List<Long>> videoToUsers,
                                   long builtAtMs) {
            this.userVideoMatrix = userVideoMatrix == null ? Collections.emptyMap() : userVideoMatrix;
            this.videoToUsers = videoToUsers == null ? Collections.emptyMap() : videoToUsers;
            this.builtAtMs = builtAtMs;
        }
    }

    private static class CachedMfModel {
        final double[][] P;
        final double[][] Q;
        final Map<Long, Integer> userIndex;
        final List<String> videoIds;
        final int factors;
        final long builtAtMs;

        private CachedMfModel(double[][] p,
                              double[][] q,
                              Map<Long, Integer> userIndex,
                              List<String> videoIds,
                              int factors,
                              long builtAtMs) {
            this.P = p;
            this.Q = q;
            this.userIndex = userIndex == null ? Collections.emptyMap() : userIndex;
            this.videoIds = videoIds == null ? Collections.emptyList() : videoIds;
            this.factors = factors;
            this.builtAtMs = builtAtMs;
        }
    }
}



