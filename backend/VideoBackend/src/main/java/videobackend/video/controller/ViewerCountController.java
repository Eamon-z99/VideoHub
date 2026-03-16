package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.ViewerCountService;

import java.util.Map;

/**
 * 在看人数（轻量心跳版）
 * - POST /api/watch/heartbeat  body: { videoId, clientId } -> { success, count }
 * - GET  /api/watch/count?videoId=xxx -> { success, count }
 */
@RestController
@RequestMapping("/api/watch")
public class ViewerCountController {

    private final ViewerCountService viewerCountService;

    public ViewerCountController(ViewerCountService viewerCountService) {
        this.viewerCountService = viewerCountService;
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody Map<String, Object> body) {
        String videoId = body == null ? null : String.valueOf(body.getOrDefault("videoId", "")).trim();
        String clientId = body == null ? null : String.valueOf(body.getOrDefault("clientId", "")).trim();

        if (videoId == null || videoId.isEmpty() || clientId == null || clientId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "videoId/clientId 不能为空"
            ));
        }

        int count = viewerCountService.heartbeat(videoId, clientId);
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam("videoId") String videoId) {
        if (videoId == null || videoId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "videoId 不能为空"
            ));
        }
        int count = viewerCountService.count(videoId.trim());
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }
}


