package videobackend.video.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import videobackend.video.util.JwtUtil;

/**
 * 管理端鉴权：仅接受 {@link JwtUtil#PRINCIPAL_TYPE_ADMIN} 的 JWT，并校验 {@code admins} 表启用状态。
 */
@Service
public class AdminAuthService {

    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    public AdminAuthService(JwtUtil jwtUtil, JdbcTemplate jdbcTemplate) {
        this.jwtUtil = jwtUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 返回管理员 admins.id；非管理端 Token 或未启用返回 null。
     */
    public Long requireAdmin(HttpServletRequest request) {
        Long adminId = getAdminIdFromRequest(request);
        if (adminId == null) {
            return null;
        }
        return isActiveAdmin(adminId) ? adminId : null;
    }

    public boolean isActiveAdmin(Long adminId) {
        if (adminId == null) {
            return false;
        }
        try {
            Integer n = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM admins
                    WHERE id = ?
                      AND (status IS NULL OR status = 1)
                    """,
                    Integer.class,
                    adminId
            );
            return n != null && n > 0;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false;
        }
    }

    private Long getAdminIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (!StringUtils.hasText(token)) {
                return null;
            }
            return jwtUtil.getAdminIdFromToken(token);
        }
        return null;
    }
}
