package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.AvatarSubmissionService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/avatar-submissions")
public class AdminAvatarSubmissionController {

    private final AvatarSubmissionService avatarSubmissionService;
    private final AdminAuthService adminAuthService;

    public AdminAvatarSubmissionController(AvatarSubmissionService avatarSubmissionService,
                                           AdminAuthService adminAuthService) {
        this.avatarSubmissionService = avatarSubmissionService;
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
                "data", avatarSubmissionService.listPending(page, pageSize)
        ));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(HttpServletRequest request,
                                     @PathVariable("id") Long id,
                                     @RequestBody(required = false) Map<String, Object> body) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        String comment = body != null ? String.valueOf(body.getOrDefault("comment", "")).trim() : null;
        try {
            avatarSubmissionService.approve(id, adminId, comment);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(HttpServletRequest request,
                                    @PathVariable("id") Long id,
                                    @RequestBody(required = false) Map<String, Object> body) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        String comment = body != null ? String.valueOf(body.getOrDefault("comment", "")).trim() : null;
        try {
            avatarSubmissionService.reject(id, adminId, comment);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

