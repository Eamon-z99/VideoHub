package videobackend.video.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import videobackend.video.service.UserDailyRewardService;
import videobackend.video.service.UserLevelService;
import videobackend.video.util.JwtUtil;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserLevelService userLevelService;
    private final UserDailyRewardService userDailyRewardService;

    public JwtInterceptor(JwtUtil jwtUtil, UserLevelService userLevelService, UserDailyRewardService userDailyRewardService) {
        this.jwtUtil = jwtUtil;
        this.userLevelService = userLevelService;
        this.userDailyRewardService = userDailyRewardService;
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
            path.startsWith("/feed-images/") ||
            path.startsWith("/error") ||
            isPublicVideoRead ||
            isPublicSearchRead ||
            // 动态列表 GET（含个人主页 authorId 查询）允许未登录访问
            (path.equals("/api/feeds") && "GET".equals(request.getMethod())) ||
            // 单条动态详情 GET：/api/feeds/数字
            ("GET".equals(request.getMethod()) && path.matches("/api/feeds/\\d+")) ||
            // 个人关注/粉丝列表（公开可读，登录则带 iFollow）
            ("GET".equals(request.getMethod()) && path.matches("/api/follows/profile/\\d+/following")) ||
            ("GET".equals(request.getMethod()) && path.matches("/api/follows/profile/\\d+/fans")) ||
            // 用户公开资料、公开等级（访客可看个人主页）
            ("GET".equals(request.getMethod()) && path.matches("/api/user/profile/public/\\d+")) ||
            ("GET".equals(request.getMethod()) && path.matches("/api/user/level/public/\\d+"))) {
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

        // 方案A：跨天仍在线，也能在“当天第一次已登录请求”时补发每日登录奖励
        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                Long userId = null;
                if (userIdObj instanceof Number) userId = ((Number) userIdObj).longValue();
                if (userId == null && userIdObj instanceof String) {
                    try {
                        userId = Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignore) {
                        userId = null;
                    }
                }

                if (userId != null) {
                    userLevelService.awardLoginExp(userId);
                    userDailyRewardService.awardDailyLoginCoin(userId);
                }
            }
        } catch (Exception e) {
            // 不影响正常请求；仅记录日志方便排查
            logger.debug("daily login grant skipped: {}", e.getMessage());
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

