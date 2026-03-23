package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.VideoSubmissionService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/video-submissions")
public class AdminVideoSubmissionController {

    private final VideoSubmissionService videoSubmissionService;
    private final AdminAuthService adminAuthService;

    public AdminVideoSubmissionController(VideoSubmissionService videoSubmissionService,
                                          AdminAuthService adminAuthService) {
        this.videoSubmissionService = videoSubmissionService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/pending")
    public ResponseEntity<?> listPending(HttpServletRequest request,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int pageSize) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", videoSubmissionService.listPending(page, pageSize)
        ));
    }

    @GetMapping("/approved-unpublished")
    public ResponseEntity<?> listApprovedUnpublished(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", videoSubmissionService.listApprovedUnpublished(page, pageSize)
        ));
    }

    /**
     * 单条投稿播放地址（管理端观看按钮兜底）。
     */
    @GetMapping("/{submissionId}/play-url")
    public ResponseEntity<?> getPlayUrl(HttpServletRequest request,
                                        @PathVariable String submissionId) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", videoSubmissionService.getSubmissionPlayInfo(submissionId)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "投稿不存在或地址不可用"));
        }
    }

    /**
     * 管理端：发布校验（帮助判断是否写入 videos 表 & 文件是否存在）
     */
    @GetMapping("/{submissionId}/published-check")
    public ResponseEntity<?> publishedCheck(HttpServletRequest request,
                                              @PathVariable String submissionId) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        try {
            return ResponseEntity.ok(videoSubmissionService.getPublishedVideoCheck(submissionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "校验失败"));
        }
    }

    @PostMapping("/{submissionId}/approve")
    public ResponseEntity<?> approve(HttpServletRequest request,
                                     @PathVariable String submissionId,
                                     @RequestBody(required = false) Map<String, Object> body) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        String comment = body != null ? String.valueOf(body.getOrDefault("comment", "")).trim() : null;
        try {
            videoSubmissionService.approve(submissionId, adminId, comment);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{submissionId}/reject")
    public ResponseEntity<?> reject(HttpServletRequest request,
                                    @PathVariable String submissionId,
                                    @RequestBody(required = false) Map<String, Object> body) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        String comment = body != null ? String.valueOf(body.getOrDefault("comment", "")).trim() : null;
        try {
            videoSubmissionService.reject(submissionId, adminId, comment);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 发布单条投稿（含定时发布）：默认 force=false，未到定时发布时间会拒绝。
     */
    @PostMapping("/{submissionId}/publish")
    public ResponseEntity<?> publishOne(HttpServletRequest request,
                                        @PathVariable String submissionId,
                                        @RequestParam(defaultValue = "0") int force) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        try {
            String videoId = videoSubmissionService.publishOne(submissionId, adminId, force == 1);
            return ResponseEntity.ok(Map.of("success", true, "videoId", videoId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 批量发布到点投稿：用于定时发布的“扫表发布”。
     */
    @PostMapping("/publish-due")
    public ResponseEntity<?> publishDue(HttpServletRequest request,
                                        @RequestParam(defaultValue = "50") int limit) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", videoSubmissionService.publishDue(limit, adminId)
        ));
    }

    // 鉴权逻辑统一下沉到 AdminAuthService
}

