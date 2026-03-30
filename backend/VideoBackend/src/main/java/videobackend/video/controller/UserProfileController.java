package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import videobackend.video.model.User;
import videobackend.video.service.MediaUrlResolver;
import videobackend.video.service.ProfileVisitService;
import videobackend.video.service.UserLevelService;
import videobackend.video.service.UserProfileService;
import videobackend.video.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ProfileVisitService profileVisitService;
    private final UserLevelService userLevelService;
    private final JwtUtil jwtUtil;
    private final MediaUrlResolver mediaUrlResolver;

    public UserProfileController(UserProfileService userProfileService,
                                 ProfileVisitService profileVisitService,
                                 UserLevelService userLevelService,
                                 JwtUtil jwtUtil,
                                 MediaUrlResolver mediaUrlResolver) {
        this.userProfileService = userProfileService;
        this.profileVisitService = profileVisitService;
        this.userLevelService = userLevelService;
        this.jwtUtil = jwtUtil;
        this.mediaUrlResolver = mediaUrlResolver;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        return userProfileService.getUserById(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(buildUserPayload(user, userId)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("success", false, "message", "用户不存在")));
    }

    /**
     * 公开资料（访客可访问）：头像、昵称、签名等，不含邮箱/登录账号
     */
    @GetMapping("/public/{userId}")
    public ResponseEntity<?> getPublicProfile(@PathVariable Long userId) {
        return userProfileService.getUserById(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(buildPublicUserPayload(user, userId)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("success", false, "message", "用户不存在")));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> updateAvatar(HttpServletRequest request,
                                          @RequestPart("file") MultipartFile file) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }
            User user = userProfileService.updateAvatar(userId, file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "avatar", mediaUrlResolver.resolveAvatar(user.getAvatar())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "头像上传失败，请稍后重试"));
        }
    }

    @PostMapping("/bio")
    public ResponseEntity<?> updateBio(HttpServletRequest request,
                                       @RequestBody Map<String, String> body) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        String bio = body.getOrDefault("bio", "").trim();
        User user = userProfileService.updateBio(userId, bio);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "bio", user.getBio()
        ));
    }

    @PostMapping("/visit")
    public ResponseEntity<?> recordProfileVisit(HttpServletRequest request,
                                                @RequestBody Map<String, Object> body) {
        Object rawProfileUserId = body.get("profileUserId");
        if (rawProfileUserId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少 profileUserId"));
        }
        Long profileUserId = Long.valueOf(rawProfileUserId.toString());
        Long visitorId = getUserIdFromRequest(request);

        if (visitorId != null) {
            profileVisitService.recordVisit(visitorId, profileUserId);
        }
        return ResponseEntity.ok(Map.of("success", true));
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
                if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> buildUserPayload(User user, Long userIdForLevel) {
        // Map.of 不允许 null，这里用可写 Map，避免 email/avatar/bio 为空时 500
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        payload.put("account", user.getAccount());
        payload.put("email", user.getEmail());
        payload.put("avatar", mediaUrlResolver.resolveAvatar(user.getAvatar()));
        payload.put("bio", user.getBio());
        payload.put("createTime", user.getCreateTime());
        putLevel(payload, userIdForLevel);
        return payload;
    }

    private Map<String, Object> buildPublicUserPayload(User user, Long userIdForLevel) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        payload.put("avatar", mediaUrlResolver.resolveAvatar(user.getAvatar()));
        payload.put("bio", user.getBio() != null ? user.getBio() : "");
        payload.put("createTime", user.getCreateTime());
        putLevel(payload, userIdForLevel);
        return payload;
    }

    private void putLevel(Map<String, Object> payload, Long userId) {
        try {
            Map<String, Object> prog = userLevelService.getUserLevelProgress(userId);
            Object lv = prog.get("level");
            payload.put("level", lv instanceof Number ? ((Number) lv).intValue() : 0);
        } catch (Exception e) {
            payload.put("level", 0);
        }
    }
}


