package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import videobackend.video.dto.LoginRequest;
import videobackend.video.util.JwtUtil;

import java.util.Map;

/**
 * 管理端独立账号登录（admins 表，与普通 users 无关联）。
 */
@Service
public class AdminLoginService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminLoginService(JdbcTemplate jdbcTemplate, JwtUtil jwtUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(LoginRequest request) {
        if (request.getAccount() == null || request.getAccount().isBlank()) {
            throw new RuntimeException("账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        String account = request.getAccount().trim();

        Map<String, Object> row;
        try {
            row = jdbcTemplate.queryForMap(
                    """
                    SELECT id, account, password, display_name, status
                    FROM admins
                    WHERE account = ?
                    """,
                    account
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new RuntimeException("账号或密码错误");
        }

        Object st = row.get("status");
        int status = (st instanceof Number) ? ((Number) st).intValue() : 1;
        if (status != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        String storedPassword = String.valueOf(row.get("password"));
        if (!StringUtils.hasText(storedPassword)) {
            throw new RuntimeException("账号或密码错误");
        }
        if (storedPassword.startsWith("$10$")) {
            storedPassword = "$2a" + storedPassword;
        }
        if (!storedPassword.startsWith("$2")) {
            throw new RuntimeException("账号或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), storedPassword)) {
            throw new RuntimeException("账号或密码错误");
        }

        Long adminId = ((Number) row.get("id")).longValue();
        String displayName = row.get("display_name") != null ? String.valueOf(row.get("display_name")) : "";
        String token = jwtUtil.generateAdminToken(adminId, account, displayName);

        return Map.of(
                "success", true,
                "token", token,
                "adminId", adminId,
                "account", account,
                "displayName", displayName,
                "isAdmin", true
        );
    }
}
