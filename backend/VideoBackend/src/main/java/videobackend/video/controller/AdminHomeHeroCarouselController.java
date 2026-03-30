package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.HomeHeroCarouselService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/home-hero")
@CrossOrigin(origins = "*")
public class AdminHomeHeroCarouselController {

    private final HomeHeroCarouselService homeHeroCarouselService;
    private final AdminAuthService adminAuthService;

    public AdminHomeHeroCarouselController(HomeHeroCarouselService homeHeroCarouselService,
                                            AdminAuthService adminAuthService) {
        this.homeHeroCarouselService = homeHeroCarouselService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public ResponseEntity<?> get(HttpServletRequest request) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }

        List<String> ids = homeHeroCarouselService.getConfiguredVideoIds();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "videoIds", ids
                )
        ));
    }

    public static class SavePayload {
        public List<String> videoIds;
    }

    @PostMapping
    public ResponseEntity<?> save(HttpServletRequest request,
                                    @RequestBody(required = false) SavePayload payload) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }

        List<String> videoIds = payload == null ? null : payload.videoIds;
        homeHeroCarouselService.setConfiguredVideoIds(videoIds);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

