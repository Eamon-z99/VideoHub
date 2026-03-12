package videobackend.video.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import videobackend.video.model.VideoItem;
import videobackend.video.service.RecommendationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐系统离线实验接口：
 * - 不做时间预算限制；
 * - 返回完整算法结果和运行耗时，便于论文实验记录。
 */
@RestController
@RequestMapping("/api/recommendations/experiment")
public class RecommendationExperimentController {

    private final RecommendationService recommendationService;

    public RecommendationExperimentController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> userCfFull(@PathVariable Long userId,
                                          @RequestParam(defaultValue = "50") int limit) {
        long start = System.currentTimeMillis();
        List<VideoItem> list = recommendationService.recommendForUserFull(userId, limit);
        long duration = System.currentTimeMillis() - start;
        Map<String, Object> resp = new HashMap<>();
        resp.put("algorithm", "user-based-cf-full");
        resp.put("userId", userId);
        resp.put("limit", limit);
        resp.put("durationMs", duration);
        resp.put("total", list.size());
        resp.put("list", list);
        return resp;
    }

    @GetMapping("/item/{userId}")
    public Map<String, Object> itemCfFull(@PathVariable Long userId,
                                          @RequestParam(defaultValue = "50") int limit) {
        long start = System.currentTimeMillis();
        List<VideoItem> list = recommendationService.recommendByItemFull(userId, limit);
        long duration = System.currentTimeMillis() - start;
        Map<String, Object> resp = new HashMap<>();
        resp.put("algorithm", "item-based-cf-full");
        resp.put("userId", userId);
        resp.put("limit", limit);
        resp.put("durationMs", duration);
        resp.put("total", list.size());
        resp.put("list", list);
        return resp;
    }

    @GetMapping("/mf/{userId}")
    public Map<String, Object> mfFull(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "50") int limit,
                                      @RequestParam(defaultValue = "20") int epochs) {
        long start = System.currentTimeMillis();
        List<VideoItem> list = recommendationService.recommendByMatrixFactorizationFull(userId, limit, epochs);
        long duration = System.currentTimeMillis() - start;
        Map<String, Object> resp = new HashMap<>();
        resp.put("algorithm", "matrix-factorization-full");
        resp.put("userId", userId);
        resp.put("limit", limit);
        resp.put("epochs", epochs);
        resp.put("durationMs", duration);
        resp.put("total", list.size());
        resp.put("list", list);
        return resp;
    }
}


