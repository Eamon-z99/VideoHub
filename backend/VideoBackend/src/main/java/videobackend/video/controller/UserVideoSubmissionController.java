package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import videobackend.video.service.VideoSubmissionService;
import videobackend.video.util.JwtUtil;

import java.io.IOException;
import java.util.Map;

/**
 * 用户侧投稿信息更新：用于“先上传视频 -> 再编辑信息”的两步投稿流程。
 */
@RestController
@RequestMapping("/api/db/video-submissions")
public class UserVideoSubmissionController {

    private final VideoSubmissionService videoSubmissionService;
    private final JwtUtil jwtUtil;

    public UserVideoSubmissionController(VideoSubmissionService videoSubmissionService, JwtUtil jwtUtil) {
        this.videoSubmissionService = videoSubmissionService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 更新投稿信息（不重新上传视频文件）。
     * 支持可选更新封面图片 cover。
     */
    @PostMapping("/{submissionId}/update")
    public ResponseEntity<?> update(HttpServletRequest request,
                                    @PathVariable String submissionId,
                                    @RequestPart(value = "cover", required = false) MultipartFile cover,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false, name = "copyright") String copyright,
                                    @RequestParam(required = false, name = "partition") String partition,
                                    @RequestParam(required = false, name = "tags") String tagsJson,
                                    @RequestParam(required = false) String scheduleEnabled,
                                    @RequestParam(required = false) String schedulePublishAt,
                                    @RequestParam(required = false) String collectionEnabled,
                                    @RequestParam(required = false) String collectionName,
                                    @RequestParam(required = false) String allowSecondCreation,
                                    @RequestParam(required = false) String commercialPromotion,
                                    @RequestParam(required = false, defaultValue = "0") int submitNow) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }
            videoSubmissionService.updateSubmission(
                    submissionId,
                    userId,
                    title,
                    description,
                    cover,
                    copyright,
                    partition,
                    tagsJson,
                    scheduleEnabled,
                    schedulePublishAt,
                    collectionEnabled,
                    collectionName,
                    allowSecondCreation,
                    commercialPromotion,
                    submitNow == 1
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "保存失败，请稍后重试"));
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

