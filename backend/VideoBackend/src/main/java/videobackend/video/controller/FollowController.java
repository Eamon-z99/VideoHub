package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.FollowService;
import videobackend.video.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;
    private final JwtUtil jwtUtil;

    public FollowController(FollowService followService, JwtUtil jwtUtil) {
        this.followService = followService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 关注用户
     */
    @PostMapping("/follow")
    public ResponseEntity<?> followUser(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long followerId = getUserIdFromRequest(request);
            if (followerId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            Object followingIdObj = body.get("followingId");
            if (followingIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少被关注用户ID"));
            }

            Long followingId;
            if (followingIdObj instanceof Number) {
                followingId = ((Number) followingIdObj).longValue();
            } else {
                followingId = Long.parseLong(followingIdObj.toString());
            }

            boolean success = followService.followUser(followerId, followingId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "关注成功"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "已关注该用户"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "关注失败: " + e.getMessage()));
        }
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollowUser(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long followerId = getUserIdFromRequest(request);
            if (followerId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            Object followingIdObj = body.get("followingId");
            if (followingIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少被关注用户ID"));
            }

            Long followingId;
            if (followingIdObj instanceof Number) {
                followingId = ((Number) followingIdObj).longValue();
            } else {
                followingId = Long.parseLong(followingIdObj.toString());
            }

            boolean success = followService.unfollowUser(followerId, followingId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "取消关注成功"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "未关注该用户"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "取消关注失败: " + e.getMessage()));
        }
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkFollow(HttpServletRequest request, @RequestParam Long followingId) {
        try {
            Long followerId = getUserIdFromRequest(request);
            if (followerId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            boolean isFollowing = followService.isFollowing(followerId, followingId);
            return ResponseEntity.ok(Map.of("success", true, "isFollowing", isFollowing));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的关注列表（仅ID）
     */
    @GetMapping("/following")
    public ResponseEntity<?> getFollowingList(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            List<Long> followingIds = followService.getFollowingIds(userId);
            return ResponseEntity.ok(Map.of("success", true, "followingIds", followingIds));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取关注列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取关注用户的详细信息列表（包含头像、名称等）
     */
    @GetMapping("/following/users")
    public ResponseEntity<?> getFollowingUsers(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            List<Map<String, Object>> followingUsers = followService.getFollowingUsers(userId);
            return ResponseEntity.ok(Map.of("success", true, "users", followingUsers));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取关注用户列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户统计信息（关注数、粉丝数、视频数）
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(@RequestParam Long userId) {
        try {
            Map<String, Object> stats = followService.getUserStats(userId);
            return ResponseEntity.ok(Map.of("success", true, "stats", stats));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取统计信息失败: " + e.getMessage()));
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

