package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.VideoCollectionService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

/**
 * 投稿合集：列表（公开）、创建/修改/删除（需登录且为本人）
 */
@RestController
@RequestMapping("/api/db/video-collections")
public class VideoCollectionController {

    private final VideoCollectionService videoCollectionService;
    private final JwtUtil jwtUtil;

    public VideoCollectionController(VideoCollectionService videoCollectionService, JwtUtil jwtUtil) {
        this.videoCollectionService = videoCollectionService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 播放页：根据当前视频 ID 查询所属合集及列表（无合集则 inCollection=false）。
     */
    @GetMapping("/by-video/{videoId}")
    public ResponseEntity<?> contextByVideo(HttpServletRequest request, @PathVariable String videoId) {
        if (!StringUtils.hasText(videoId)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少视频ID"));
        }
        Long uid = getUserIdFromRequest(request);
        Map<String, Object> data = videoCollectionService.getContextByVideoId(videoId.trim(), uid);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    /**
     * 个人中心「我追的合集」：某用户订阅的合集列表（公开可读）。
     */
    @GetMapping("/subscribed/list")
    public ResponseEntity<?> listSubscribed(@RequestParam long userId) {
        if (userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无效的用户ID"));
        }
        List<Map<String, Object>> list = videoCollectionService.listSubscribedCollections(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("list", list)));
    }

    @PostMapping("/{collectionId}/subscribe")
    public ResponseEntity<?> subscribe(HttpServletRequest request, @PathVariable long collectionId) {
        Long uid = getUserIdFromRequest(request);
        if (uid == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        try {
            videoCollectionService.subscribe(uid, collectionId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{collectionId}/subscribe")
    public ResponseEntity<?> unsubscribe(HttpServletRequest request, @PathVariable long collectionId) {
        Long uid = getUserIdFromRequest(request);
        if (uid == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        try {
            videoCollectionService.unsubscribe(uid, collectionId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam long userId) {
        if (userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无效的用户ID"));
        }
        Map<String, Object> data = videoCollectionService.listWithCounts(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request,
                                    @RequestBody Map<String, Object> body) {
        Long uid = getUserIdFromRequest(request);
        if (uid == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        String name = body != null && body.get("name") != null ? String.valueOf(body.get("name")) : "";
        String description = body != null && body.get("description") != null ? String.valueOf(body.get("description")) : null;
        if (!StringUtils.hasText(description)) {
            description = null;
        }
        try {
            long id = videoCollectionService.create(uid, name, description);
            return ResponseEntity.ok(Map.of("success", true, "data", Map.of("id", id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<?> update(HttpServletRequest request,
                                    @PathVariable long collectionId,
                                    @RequestBody Map<String, Object> body) {
        Long uid = getUserIdFromRequest(request);
        if (uid == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        String name = body != null && body.get("name") != null ? String.valueOf(body.get("name")) : "";
        String description = body != null && body.get("description") != null ? String.valueOf(body.get("description")) : null;
        if (!StringUtils.hasText(description)) {
            description = null;
        }
        try {
            videoCollectionService.update(uid, collectionId, name, description);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<?> delete(HttpServletRequest request, @PathVariable long collectionId) {
        Long uid = getUserIdFromRequest(request);
        if (uid == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        try {
            videoCollectionService.delete(uid, collectionId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 调整已发布视频所属投稿合集：body.collectionId 为 null 表示移出合集。
     */
    @PutMapping("/videos/{videoId}/collection")
    public ResponseEntity<?> setVideoCollection(HttpServletRequest request,
                                                @PathVariable String videoId,
                                                @RequestBody(required = false) Map<String, Object> body) {
        Long uid = getUserIdFromRequest(request);
        if (uid == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        Long collectionId = null;
        if (body != null && body.containsKey("collectionId")) {
            Object raw = body.get("collectionId");
            if (raw != null) {
                if (raw instanceof Number) {
                    collectionId = ((Number) raw).longValue();
                } else {
                    String s = String.valueOf(raw).trim();
                    if (!s.isEmpty()) {
                        try {
                            collectionId = Long.parseLong(s);
                        } catch (NumberFormatException e) {
                            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "合集ID无效"));
                        }
                    }
                }
            }
        }
        try {
            videoCollectionService.setVideoCollection(uid, videoId, collectionId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                }
                if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return null;
    }
}
