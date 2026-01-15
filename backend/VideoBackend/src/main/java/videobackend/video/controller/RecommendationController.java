package videobackend.video.controller;

import org.springframework.web.bind.annotation.*;
import videobackend.video.model.VideoItem;
import videobackend.video.service.RecommendationService;

import java.util.List;
import java.util.Map;

/**
 * 推荐系统控制器
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * 为用户推荐视频（基于用户的协同过滤）
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> recommendForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<VideoItem> recommendations = recommendationService.recommendForUser(userId, limit);
        return Map.of(
                "list", recommendations,
                "total", recommendations.size(),
                "algorithm", "user-based-cf"
        );
    }

    /**
     * 基于物品的推荐
     */
    @GetMapping("/item/{userId}")
    public Map<String, Object> recommendByItem(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<VideoItem> recommendations = recommendationService.recommendByItem(userId, limit);
        return Map.of(
                "list", recommendations,
                "total", recommendations.size(),
                "algorithm", "item-based-cf"
        );
    }
}



