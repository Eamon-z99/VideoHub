package videobackend.video.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:VideoHubSecretKeyForJWTTokenGeneration2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static final String CLAIM_PRINCIPAL_TYPE = "principalType";
    public static final String PRINCIPAL_TYPE_USER = "USER";
    public static final String PRINCIPAL_TYPE_ADMIN = "ADMIN";
    public static final String CLAIM_ADMIN_ID = "adminId";

    public String generateToken(Long userId, String username, String account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("account", account);
        claims.put(CLAIM_PRINCIPAL_TYPE, PRINCIPAL_TYPE_USER);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject("user:" + userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 管理端独立账号 JWT（不含 userId，与普通用户 Token 区分）。
     */
    public String generateAdminToken(Long adminId, String account, String displayName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ADMIN_ID, adminId);
        claims.put("account", account);
        claims.put("username", displayName != null && !displayName.isBlank() ? displayName : account);
        claims.put(CLAIM_PRINCIPAL_TYPE, PRINCIPAL_TYPE_ADMIN);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject("admin:" + adminId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        if (PRINCIPAL_TYPE_ADMIN.equals(claims.get(CLAIM_PRINCIPAL_TYPE))) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        if (userId instanceof String) {
            return Long.parseLong((String) userId);
        }
        return null;
    }

    public Long getAdminIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null || !PRINCIPAL_TYPE_ADMIN.equals(claims.get(CLAIM_PRINCIPAL_TYPE))) {
            return null;
        }
        Object adminId = claims.get(CLAIM_ADMIN_ID);
        if (adminId instanceof Number) {
            return ((Number) adminId).longValue();
        }
        if (adminId instanceof String) {
            try {
                return Long.parseLong((String) adminId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 HttpServletRequest 的 Authorization 头中解析 userId，方便在各个 Controller 中复用
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        if (request == null) return null;
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return getUserIdFromToken(token);
        }
        return null;
    }
}

