package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.VideoItem;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.LocalVideoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/videos/by-ids")
@CrossOrigin(origins = "*")
public class AdminVideosByIdsController {

    private final LocalVideoService localVideoService;
    private final AdminAuthService adminAuthService;

    public AdminVideosByIdsController(LocalVideoService localVideoService,
                                       AdminAuthService adminAuthService) {
        this.localVideoService = localVideoService;
        this.adminAuthService = adminAuthService;
    }

    public static class ByIdsPayload {
        public List<String> videoIds;
    }

    @PostMapping
    public ResponseEntity<?> listByIds(HttpServletRequest request,
                                         @RequestBody(required = false) ByIdsPayload payload) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }

        List<String> videoIds = payload == null ? null : payload.videoIds;
        List<VideoItem> list = localVideoService.listByVideoIdsPreserveOrder(videoIds);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("list", list)
        ));
    }
}

