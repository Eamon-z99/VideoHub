package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.CommentItem;
import videobackend.video.service.CommentService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    public CommentController(CommentService commentService, JwtUtil jwtUtil) {
        this.commentService = commentService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 分页获取视频评论列表（顶层评论）
     */
    @GetMapping
    public Map<String, Object> list(@RequestParam String videoId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int pageSize,
                                    @RequestParam(defaultValue = "time") String sort) {
        List<CommentItem> items = commentService.listComments(videoId, page, pageSize, sort);
        long total = commentService.countComments(videoId);
        return Map.of(
                "success", true,
                "list", items,
                "page", page,
                "pageSize", pageSize,
                "sort", sort,
                "total", total
        );
    }

    /**
     * 新增评论（需要登录）
     */
    @PostMapping("/add")
    public ResponseEntity<?> add(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            Object videoIdObj = body.get("videoId");
            Object contentObj = body.get("content");

            if (videoIdObj == null || contentObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少视频ID或评论内容"));
            }

            String videoId = videoIdObj.toString();
            String content = contentObj.toString();
            if (content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "评论内容不能为空"));
            }

            Long parentId = null;
            if (body.get("parentId") != null) {
                parentId = Long.valueOf(body.get("parentId").toString());
            }

            CommentItem comment = commentService.addComment(userId, videoId, content, parentId);
            return ResponseEntity.ok(Map.of("success", true, "data", comment));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "发表评论失败: " + e.getMessage()));
        }
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like")
    public ResponseEntity<?> like(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            Object commentIdObj = body.get("commentId");
            if (commentIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少评论ID"));
            }
            Long commentId = Long.valueOf(commentIdObj.toString());

            boolean created = commentService.likeComment(userId, commentId);
            long likeCount = commentService.getLikeCount(commentId);
            if (created) {
                return ResponseEntity.ok(Map.of("success", true, "message", "点赞成功", "likeCount", likeCount));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "已点赞过该评论", "likeCount", likeCount));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "点赞失败: " + e.getMessage()));
        }
    }

    /**
     * 取消点赞评论
     */
    @PostMapping("/unlike")
    public ResponseEntity<?> unlike(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            Object commentIdObj = body.get("commentId");
            if (commentIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少评论ID"));
            }
            Long commentId = Long.valueOf(commentIdObj.toString());

            boolean success = commentService.unlikeComment(userId, commentId);
            long likeCount = commentService.getLikeCount(commentId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "已取消点赞", "likeCount", likeCount));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "尚未点赞该评论", "likeCount", likeCount));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "取消点赞失败: " + e.getMessage()));
        }
    }

    /**
     * 获取某条评论下的所有回复
     */
    @GetMapping("/replies")
    public Map<String, Object> listReplies(@RequestParam String videoId,
                                           @RequestParam Long parentId) {
        List<CommentItem> items = commentService.listReplies(videoId, parentId);
        return Map.of(
                "success", true,
                "list", items
        );
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            var claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                }
                if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return null;
    }
}


