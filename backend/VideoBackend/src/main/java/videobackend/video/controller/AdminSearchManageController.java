package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.AdminSearchManageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/search")
public class AdminSearchManageController {

    private final AdminSearchManageService adminSearchManageService;
    private final AdminAuthService adminAuthService;

    public AdminSearchManageController(AdminSearchManageService adminSearchManageService,
                                       AdminAuthService adminAuthService) {
        this.adminSearchManageService = adminSearchManageService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/keyword-stats")
    public ResponseEntity<?> keywordStats(HttpServletRequest request,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int pageSize,
                                         @RequestParam(required = false) String keyword) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", adminSearchManageService.listKeywordStats(page, pageSize, keyword)
        ));
    }

    @GetMapping("/keyword-events")
    public ResponseEntity<?> keywordEvents(HttpServletRequest request,
                                          @RequestParam String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int pageSize) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", adminSearchManageService.listKeywordEvents(keyword, page, pageSize)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/user-events")
    public ResponseEntity<?> userEvents(HttpServletRequest request,
                                        @RequestParam Long userId,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int pageSize) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", adminSearchManageService.listUserEvents(userId, page, pageSize)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/hot-slots")
    public ResponseEntity<?> hotSlots(HttpServletRequest request) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("list", adminSearchManageService.getHotSlots())
        ));
    }

    @PostMapping("/hot-slots")
    public ResponseEntity<?> saveHotSlots(HttpServletRequest request,
                                          @RequestBody Map<String, Object> body) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        Object listObj = body == null ? null : body.get("list");
        if (!(listObj instanceof List<?> raw)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "list 不能为空"));
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) (List<?>) raw;
        try {
            adminSearchManageService.saveHotSlots(list);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

