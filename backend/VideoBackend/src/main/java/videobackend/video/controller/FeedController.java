package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.FeedItem;
import videobackend.video.service.FeedService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feeds")
@CrossOrigin(origins = "*")
public class FeedController {

    private final FeedService feedService;
    private final JwtUtil jwtUtil;

    public FeedController(FeedService feedService, JwtUtil jwtUtil) {
        this.feedService = feedService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取关注用户的动态列表
     */
    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int pageSize,
                                     @RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) Boolean followingOnly,
                                     @RequestParam(required = false) Long followingId) {
        List<FeedItem> items;
        long total;

        if (followingOnly != null && followingOnly && userId != null) {
            // 只返回关注用户的动态
            items = feedService.listPageByFollowing(userId, followingId, page, pageSize);
            total = feedService.countByFollowing(userId, followingId);
        } else {
            // 如果没有指定关注筛选，返回空列表
            items = List.of();
            total = 0;
        }

        return Map.of(
                "list", items,
                "page", page,
                "pageSize", pageSize,
                "total", total
        );
    }

    /**
     * 创建动态
     */
    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            String title = (String) body.get("title");
            String content = (String) body.get("content");
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) body.get("images");

            boolean hasText = content != null && !content.trim().isEmpty();
            boolean hasImages = images != null && !images.isEmpty();
            if (!hasText && !hasImages) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "请输入动态内容或上传图片"));
            }

            FeedItem feed = feedService.createFeed(userId, title, content, images);
            return ResponseEntity.ok(Map.of("success", true, "data", feed));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "创建动态失败: " + e.getMessage()));
        }
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/{feedId}")
    public ResponseEntity<?> delete(HttpServletRequest request, @PathVariable Long feedId) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            boolean deleted = feedService.deleteFeed(feedId, userId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
            } else {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权删除该动态"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "删除失败: " + e.getMessage()));
        }
    }

    /**
     * 点赞动态
     */
    @PostMapping("/{feedId}/like")
    public ResponseEntity<?> like(HttpServletRequest request, @PathVariable Long feedId) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            boolean created = feedService.likeFeed(feedId, userId);
            if (created) {
                return ResponseEntity.ok(Map.of("success", true, "message", "点赞成功"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "已点赞过该动态"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "点赞失败: " + e.getMessage()));
        }
    }

    /**
     * 取消点赞
     */
    @PostMapping("/{feedId}/unlike")
    public ResponseEntity<?> unlike(HttpServletRequest request, @PathVariable Long feedId) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            boolean removed = feedService.unlikeFeed(feedId, userId);
            if (removed) {
                return ResponseEntity.ok(Map.of("success", true, "message", "取消点赞成功"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "未点赞过该动态"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "取消点赞失败: " + e.getMessage()));
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

