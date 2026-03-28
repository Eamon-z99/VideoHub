package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.UserLevelService;
import videobackend.video.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/user/level")
@CrossOrigin(origins = "*")
public class UserLevelController {

    private final UserLevelService userLevelService;
    private final JwtUtil jwtUtil;

    public UserLevelController(UserLevelService userLevelService, JwtUtil jwtUtil) {
        this.userLevelService = userLevelService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取用户等级进度（给前端顶部下拉展示用）
     */
    @GetMapping("/progress")
    public ResponseEntity<?> getProgress(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            return ResponseEntity.ok(userLevelService.getUserLevelProgress(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取等级进度失败: " + e.getMessage()));
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) return ((Number) userIdObj).longValue();
                if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignore) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }
}

