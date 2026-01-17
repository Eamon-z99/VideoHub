package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.PlayHistoryItem;
import videobackend.video.service.PlayHistoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class PlayHistoryController {

    private final PlayHistoryService playHistoryService;

    public PlayHistoryController(PlayHistoryService playHistoryService) {
        this.playHistoryService = playHistoryService;
    }

    /**
     * 记录播放历史
     */
    @PostMapping("/record")
    public ResponseEntity<?> recordHistory(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String videoId = request.get("videoId").toString();
            Integer playTime = request.get("playTime") != null 
                ? Integer.valueOf(request.get("playTime").toString()) : 0;
            Integer duration = request.get("duration") != null 
                ? Integer.valueOf(request.get("duration").toString()) : null;

            playHistoryService.recordPlayHistory(userId, videoId, playTime, duration);
            return ResponseEntity.ok(Map.of("success", true, "message", "记录成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户播放历史列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getHistoryList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<PlayHistoryItem> list = playHistoryService.getUserHistory(userId, page, pageSize);
            Long total = playHistoryService.getUserHistoryCount(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "list", list,
                "page", page,
                "pageSize", pageSize,
                "total", total
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "获取失败: " + e.getMessage()));
        }
    }

    /**
     * 删除单条历史记录
     */
    @DeleteMapping("/{historyId}")
    public ResponseEntity<?> deleteHistory(
            @PathVariable Long historyId,
            @RequestParam Long userId) {
        try {
            boolean deleted = playHistoryService.deleteHistory(userId, historyId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
            } else {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "记录不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "删除失败: " + e.getMessage()));
        }
    }

    /**
     * 清空所有历史记录
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearAllHistory(@RequestParam Long userId) {
        try {
            boolean cleared = playHistoryService.clearAllHistory(userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "清空成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "清空失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除历史记录
     */
    @PostMapping("/batch-delete")
    public ResponseEntity<?> batchDeleteHistory(
            @RequestParam Long userId,
            @RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> historyIds = request.get("ids");
            int deleted = playHistoryService.batchDeleteHistory(userId, historyIds);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "删除成功",
                "deletedCount", deleted
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "批量删除失败: " + e.getMessage()));
        }
    }
}

