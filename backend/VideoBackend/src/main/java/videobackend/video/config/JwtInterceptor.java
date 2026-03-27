package videobackend.video.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import videobackend.video.util.JwtUtil;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

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
        // 视频列表/详情允许未登录访问（但上传投稿必须登录）
        boolean isPublicVideoRead =
                ("GET".equals(request.getMethod()) && (
                        path.equals("/api/db/videos") ||
                        path.startsWith("/api/db/videos/top") ||
                        path.startsWith("/api/db/videos/by-uploader") ||
                        path.startsWith("/api/db/videos/")  // 详情：/api/db/videos/{videoId}
                ))
                // 搜索也允许公开（POST /api/db/videos/search）
                || (path.equals("/api/db/videos/search"));

        boolean isPublicSearchRead =
                path.startsWith("/api/db/search/") &&
                (("GET".equals(request.getMethod())) || ("POST".equals(request.getMethod())));

        if (path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/logout") ||
            path.startsWith("/api/admin/auth/login") ||
            // 弹幕列表允许未登录访问（发送仍需登录）
            (path.startsWith("/api/danmaku/") && "GET".equals(request.getMethod())) ||
            // 评论列表/评论总数允许未登录访问（新增/点赞仍需登录）
            (path.startsWith("/api/comments") && "GET".equals(request.getMethod())) ||
            // 在看人数（心跳/查询）允许公开访问
            path.startsWith("/api/watch/") ||
            path.startsWith("/local-videos/") ||
            path.startsWith("/avatars/") ||
            path.startsWith("/error") ||
            isPublicVideoRead ||
            isPublicSearchRead) {
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

