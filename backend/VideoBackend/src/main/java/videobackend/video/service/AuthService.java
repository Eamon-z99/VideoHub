package videobackend.video.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import videobackend.video.dto.LoginRequest;
import videobackend.video.dto.LoginResponse;
import videobackend.video.model.User;
import videobackend.video.repository.UserRepository;
import videobackend.video.util.JwtUtil;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public LoginResponse login(LoginRequest request) {
        // 查找用户
        Optional<User> userOpt = userRepository.findByAccount(request.getAccount());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("账号或密码错误");
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        String storedPassword = user.getPassword();
        String inputPassword = request.getPassword();
        
        // 调试日志
        System.out.println("=== 密码验证开始 ===");
        System.out.println("账号: " + request.getAccount());
        System.out.println("输入密码: " + (inputPassword != null ? "***" : "null"));
        System.out.println("输入密码长度: " + (inputPassword != null ? inputPassword.length() : 0));
        System.out.println("存储密码前10个字符: " + (storedPassword != null && storedPassword.length() > 10 ? storedPassword.substring(0, 10) + "..." : storedPassword));
        System.out.println("存储密码格式: " + (storedPassword != null && storedPassword.startsWith("$2") ? "BCrypt" : "其他"));
        
        // 检查存储的密码是否是BCrypt格式
        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            System.out.println("错误: 存储的密码为空");
            throw new RuntimeException("账号或密码错误");
        }
        
        // 检查密码格式，如果是$10$开头（缺少版本前缀），尝试修复
        if (storedPassword.startsWith("$10$")) {
            System.out.println("警告: 密码格式缺少BCrypt版本前缀，尝试修复...");
            storedPassword = "$2a" + storedPassword;
            System.out.println("修复后的密码前缀: " + storedPassword.substring(0, 7));
        }
        
        if (!storedPassword.startsWith("$2")) {
            System.out.println("错误: 存储的密码不是BCrypt格式");
            System.out.println("密码前缀: " + (storedPassword != null && storedPassword.length() > 10 ? storedPassword.substring(0, 10) : storedPassword));
            throw new RuntimeException("账号或密码错误");
        }
        
        // 输出完整的存储密码哈希（用于调试）
        System.out.println("存储的完整密码哈希: " + storedPassword);
        
        // 验证密码 - BCrypt.matches(明文密码, 加密密码)
        boolean passwordMatches = passwordEncoder.matches(inputPassword, storedPassword);
        System.out.println("BCrypt验证结果: " + passwordMatches);
        System.out.println("=== 密码验证结束 ===");
        
        if (!passwordMatches) {
            throw new RuntimeException("账号或密码错误");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getAccount());

        // 返回登录响应
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getAccount()
        );
    }
}

