package videobackend.video.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import videobackend.video.util.JwtUtil;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 允许OPTIONS请求通过（CORS预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 公开接口不需要验证
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login") || 
            path.startsWith("/api/auth/register") ||
            path.startsWith("/local-videos/") ||
            path.startsWith("/error")) {
            return true;
        }

        // 获取token
        String token = getTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

