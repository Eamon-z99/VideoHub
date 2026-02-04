package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.VideoLikeService;
import videobackend.video.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/video-likes")
public class VideoLikeController {

    private final VideoLikeService videoLikeService;
    private final JwtUtil jwtUtil;

    public VideoLikeController(VideoLikeService videoLikeService, JwtUtil jwtUtil) {
        this.videoLikeService = videoLikeService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/like")
    public ResponseEntity<?> like(HttpServletRequest request, @RequestBody Map<String, Object> body) {
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

            boolean created = videoLikeService.likeVideo(userId, videoId);
            long likeCount = videoLikeService.getLikeCount(videoId);
            if (created) {
                return ResponseEntity.ok(Map.of("success", true, "message", "点赞成功", "likeCount", likeCount));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "已点赞过该视频", "likeCount", likeCount));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "点赞失败: " + e.getMessage()));
        }
    }

    @PostMapping("/unlike")
    public ResponseEntity<?> unlike(HttpServletRequest request, @RequestBody Map<String, Object> body) {
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

            boolean success = videoLikeService.unlikeVideo(userId, videoId);
            long likeCount = videoLikeService.getLikeCount(videoId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "已取消点赞", "likeCount", likeCount));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "尚未点赞该视频", "likeCount", likeCount));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "取消点赞失败: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpServletRequest request, @RequestParam String videoId) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            boolean isLiked = videoLikeService.isLiked(userId, videoId);
            long likeCount = videoLikeService.getLikeCount(videoId);
            return ResponseEntity.ok(Map.of("success", true, "isLiked", isLiked, "likeCount", likeCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "查询失败: " + e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam String videoId) {
        try {
            long likeCount = videoLikeService.getLikeCount(videoId);
            return ResponseEntity.ok(Map.of("success", true, "likeCount", likeCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取点赞数失败: " + e.getMessage()));
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


