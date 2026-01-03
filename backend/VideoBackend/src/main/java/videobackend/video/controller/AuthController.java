package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.dto.LoginRequest;
import videobackend.video.dto.LoginResponse;
import videobackend.video.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 验证请求参数
            if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "账号不能为空"));
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "密码不能为空"));
            }

            // 执行登录
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", response.getToken(),
                    "userId", response.getUserId(),
                    "username", response.getUsername(),
                    "loginAccount", response.getLoginAccount()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "登录失败，请稍后重试"));
        }
    }

    /**
     * 用户退出登录
     * 虽然JWT是无状态的，但提供此接口用于记录日志和统一处理
     * @return 退出登录响应
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // 执行退出登录（可以在这里添加日志记录、清除服务端缓存等操作）
            authService.logout();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "退出登录成功"
            ));
        } catch (Exception e) {
            // 即使出现异常，也返回成功，因为JWT是无状态的，客户端清除token即可
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "退出登录成功"
            ));
        }
    }
}

