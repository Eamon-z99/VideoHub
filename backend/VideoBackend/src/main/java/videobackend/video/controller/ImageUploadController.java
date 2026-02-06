package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import videobackend.video.service.ImageStorageService;
import videobackend.video.util.JwtUtil;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageUploadController {

    private final ImageStorageService imageStorageService;
    private final JwtUtil jwtUtil;

    public ImageUploadController(ImageStorageService imageStorageService, JwtUtil jwtUtil) {
        this.imageStorageService = imageStorageService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 上传图片（用于动态）
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(HttpServletRequest request,
                                        @RequestPart("file") MultipartFile file) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            // 验证文件大小（限制为 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "图片大小不能超过 5MB"));
            }

            String imageUrl = imageStorageService.storeImage(userId, file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", imageUrl
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "图片上传失败，请稍后重试"));
        }
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
}

