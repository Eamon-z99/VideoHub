package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.LocalVideoService;

import java.util.List;
import java.util.Map;

import videobackend.video.model.VideoItem;

@RestController
@RequestMapping("/api/admin/videos")
@CrossOrigin(origins = "*")
public class AdminVideoListController {

    private final LocalVideoService localVideoService;
    private final AdminAuthService adminAuthService;

    public AdminVideoListController(LocalVideoService localVideoService,
                                     AdminAuthService adminAuthService) {
        this.localVideoService = localVideoService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public ResponseEntity<?> list(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword
    ) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }

        List<VideoItem> list = localVideoService.listAdminPage(page, pageSize, keyword);
        long total = localVideoService.countAdminByKeyword(keyword);

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
}

