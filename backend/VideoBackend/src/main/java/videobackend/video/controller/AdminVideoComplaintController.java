package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.VideoComplaintItem;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.VideoComplaintService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/video-complaints")
@CrossOrigin(origins = "*")
public class AdminVideoComplaintController {

    private final VideoComplaintService videoComplaintService;
    private final AdminAuthService adminAuthService;

    public AdminVideoComplaintController(VideoComplaintService videoComplaintService,
                                         AdminAuthService adminAuthService) {
        this.videoComplaintService = videoComplaintService;
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

        long total = videoComplaintService.countPending();
        List<VideoComplaintItem> list = videoComplaintService.listPending(page, pageSize);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "list", list,
                        "total", total,
                        "page", page,
                        "pageSize", pageSize
                )
        ));
    }

    @PostMapping("/{complaintId}/process")
    public ResponseEntity<?> process(HttpServletRequest request,
                                       @PathVariable Long complaintId,
                                       @RequestBody(required = false) Map<String, Object> body) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }

        String action = body != null ? String.valueOf(body.getOrDefault("action", "")) : "";
        String handlerNote = body != null ? String.valueOf(body.getOrDefault("handlerNote", "")) : null;
        if (handlerNote != null && handlerNote.trim().isEmpty()) handlerNote = null;

        videoComplaintService.processComplaint(adminId, complaintId, action, handlerNote);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

