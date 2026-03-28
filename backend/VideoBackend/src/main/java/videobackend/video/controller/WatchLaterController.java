package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.WatchLaterItem;
import videobackend.video.service.WatchLaterService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watch-later")
@CrossOrigin(origins = "*")
public class WatchLaterController {

    private final WatchLaterService watchLaterService;

    public WatchLaterController(WatchLaterService watchLaterService) {
        this.watchLaterService = watchLaterService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String videoId = body.get("videoId").toString();
            boolean ok = watchLaterService.add(userId, videoId);
            if (ok) {
                return ResponseEntity.ok(Map.of("success", true, "message", "已加入稍后再看"));
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "已在稍后再看中"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "添加失败: " + e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> remove(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String videoId = body.get("videoId").toString();
            boolean ok = watchLaterService.remove(userId, videoId);
            if (ok) {
                return ResponseEntity.ok(Map.of("success", true, "message", "已移除"));
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "未找到记录"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "移除失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeById(@PathVariable Long id, @RequestParam Long userId) {
        try {
            boolean ok = watchLaterService.removeById(userId, id);
            if (ok) {
                return ResponseEntity.ok(Map.of("success", true, "message", "已移除"));
            }
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "未找到"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "移除失败: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        try {
            List<WatchLaterItem> list = watchLaterService.list(userId, page, pageSize);
            long total = watchLaterService.countByUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "list", list != null ? list : List.of(),
                    "page", page,
                    "pageSize", pageSize,
                    "total", total
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "获取失败: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(@RequestParam Long userId, @RequestParam String videoId) {
        try {
            boolean inList = watchLaterService.contains(userId, videoId);
            return ResponseEntity.ok(Map.of("success", true, "inWatchLater", inList));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "查询失败: " + e.getMessage()));
        }
    }
}
