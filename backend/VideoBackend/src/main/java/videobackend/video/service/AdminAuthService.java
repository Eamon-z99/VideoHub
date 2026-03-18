package videobackend.video.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import videobackend.video.util.JwtUtil;

/**
 * 管理端鉴权：从 JWT 取 userId，并校验 users.is_admin=1。
 * 管理端逻辑集中在此，避免散落在各个 controller。
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
     * 返回管理员 userId；非管理员或未登录返回 null。
     */
    public Long requireAdmin(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return null;
        }
        Integer isAdmin = jdbcTemplate.queryForObject(
                "SELECT is_admin FROM users WHERE id = ?",
                Integer.class,
                userId
        );
        return (isAdmin != null && isAdmin == 1) ? userId : null;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                }
                if (userIdObj instanceof String && StringUtils.hasText((String) userIdObj)) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return null;
    }
}

