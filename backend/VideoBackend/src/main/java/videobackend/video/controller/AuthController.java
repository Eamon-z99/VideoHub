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
}

