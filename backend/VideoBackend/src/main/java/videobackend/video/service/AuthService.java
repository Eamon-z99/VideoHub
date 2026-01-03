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
        
        // 检查存储的密码是否是BCrypt格式
        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            throw new RuntimeException("账号或密码错误");
        }
        
        // 检查密码格式，如果是$10$开头（缺少版本前缀），尝试修复
        if (storedPassword.startsWith("$10$")) {
            storedPassword = "$2a" + storedPassword;
        }
        
        if (!storedPassword.startsWith("$2")) {
            throw new RuntimeException("账号或密码错误");
        }
        
        // 验证密码 - BCrypt.matches(明文密码, 加密密码)
        boolean passwordMatches = passwordEncoder.matches(inputPassword, storedPassword);
        
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

    /**
     * 用户退出登录
     * 可以在这里添加日志记录、清除服务端缓存等操作
     * 由于JWT是无状态的，主要作用是记录退出日志
     */
    public void logout() {
        // 可以在这里添加退出登录的日志记录
        // 例如：记录退出时间、IP地址等
        // 如果将来需要实现token黑名单机制，可以在这里将token加入黑名单
        
        // 目前JWT是无状态的，所以这里主要是预留接口
        // 实际退出由客户端清除token完成
    }
}

