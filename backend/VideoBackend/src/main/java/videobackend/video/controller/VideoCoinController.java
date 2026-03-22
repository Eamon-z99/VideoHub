package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.VideoCoinService;
import videobackend.video.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/video-coins")
public class VideoCoinController {

    private final VideoCoinService videoCoinService;
    private final JwtUtil jwtUtil;

    public VideoCoinController(VideoCoinService videoCoinService, JwtUtil jwtUtil) {
        this.videoCoinService = videoCoinService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/coin")
    public ResponseEntity<?> coin(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }
            Object videoIdObj = body.get("videoId");
            if (videoIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少视频ID"));
            }
            String videoId = videoIdObj.toString();

            boolean created = videoCoinService.coinVideo(userId, videoId);
            long coinCount = videoCoinService.getCoinCount(videoId);
            long coinBalance = videoCoinService.getUserCoinBalance(userId);
            if (created) {
                return ResponseEntity.ok(Map.of("success", true, "message", "投币成功", "coinCount", coinCount, "coinBalance", coinBalance));
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "已投币过该视频", "coinCount", coinCount, "coinBalance", coinBalance));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "投币失败: " + e.getMessage()));
        }
    }

    @PostMapping("/uncoin")
    public ResponseEntity<?> uncoin(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }
            Object videoIdObj = body.get("videoId");
            if (videoIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少视频ID"));
            }
            String videoId = videoIdObj.toString();

            boolean removed = videoCoinService.uncoinVideo(userId, videoId);
            long coinCount = videoCoinService.getCoinCount(videoId);
            long coinBalance = videoCoinService.getUserCoinBalance(userId);
            if (removed) {
                return ResponseEntity.ok(Map.of("success", true, "message", "已取消投币", "coinCount", coinCount, "coinBalance", coinBalance));
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "尚未投币该视频", "coinCount", coinCount, "coinBalance", coinBalance));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "取消投币失败: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpServletRequest request, @RequestParam String videoId) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }
            boolean isCoined = videoCoinService.isCoined(userId, videoId);
            long coinCount = videoCoinService.getCoinCount(videoId);
            long coinBalance = videoCoinService.getUserCoinBalance(userId);
            return ResponseEntity.ok(Map.of("success", true, "isCoined", isCoined, "coinCount", coinCount, "coinBalance", coinBalance));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "查询失败: " + e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam String videoId) {
        try {
            long coinCount = videoCoinService.getCoinCount(videoId);
            return ResponseEntity.ok(Map.of("success", true, "coinCount", coinCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取投币数失败: " + e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> balance(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }
            long coinBalance = videoCoinService.getUserCoinBalance(userId);
            return ResponseEntity.ok(Map.of("success", true, "coinBalance", coinBalance));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取硬币余额失败: " + e.getMessage()));
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) return ((Number) userIdObj).longValue();
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

