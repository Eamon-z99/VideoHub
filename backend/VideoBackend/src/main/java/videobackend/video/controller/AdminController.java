package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import videobackend.video.dto.LoginRequest;
import videobackend.video.service.AdminAuthService;
import videobackend.video.service.AdminLoginService;

import java.util.Map;

/**
 * 管理端通用接口（当前用户信息等）。
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final AdminLoginService adminLoginService;
    private final JdbcTemplate jdbcTemplate;

    public AdminController(AdminAuthService adminAuthService,
                           AdminLoginService adminLoginService,
                           JdbcTemplate jdbcTemplate) {
        this.adminAuthService = adminAuthService;
        this.adminLoginService = adminLoginService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 管理端独立账号登录（admins 表，与普通用户无关）。
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(adminLoginService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 校验当前 JWT 是否为管理端 Token，并返回 admins 展示信息。
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        Long adminId = adminAuthService.requireAdmin(request);
        if (adminId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    """
                    SELECT id, account, display_name
                    FROM admins
                    WHERE id = ?
                      AND (status IS NULL OR status = 1)
                    """,
                    adminId
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "adminId", row.get("id"),
                    "displayName", row.get("display_name") != null ? row.get("display_name") : "",
                    "loginAccount", row.get("account") != null ? row.get("account") : "",
                    "username", row.get("display_name") != null ? row.get("display_name") : "",
                    "isAdmin", true
            ));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
    }
}
