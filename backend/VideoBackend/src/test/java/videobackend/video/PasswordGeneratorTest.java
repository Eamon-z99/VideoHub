package videobackend.video;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成测试类
 * 运行此测试类可以生成BCrypt加密的密码，用于插入测试用户
 */
public class PasswordGeneratorTest {

    @Test
    public void generatePasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt密码生成工具 ===\n");
        
        // 生成几个常用测试密码
        String[] passwords = {"123456", "admin123", "test123"};
        String[] accounts = {"testuser", "admin", "testuser2"};
        String[] usernames = {"测试用户2", "管理员", "测试用户3"};
        
        for (int i = 0; i < passwords.length; i++) {
            String password = passwords[i];
            String encoded = encoder.encode(password);
            boolean matches = encoder.matches(password, encoded);
            
            System.out.println("账号: " + accounts[i]);
            System.out.println("用户名: " + usernames[i]);
            System.out.println("明文密码: " + password);
            System.out.println("BCrypt加密: " + encoded);
            System.out.println("验证结果: " + matches);
            
            // 生成SQL语句
            System.out.println("\nSQL插入语句:");
            System.out.println("INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) VALUES");
            System.out.println("('" + usernames[i] + "', '" + accounts[i] + "', '" + encoded + "', '" + accounts[i] + "@example.com', 1, NOW(), NOW());");
            System.out.println("\n" + "=".repeat(60) + "\n");
        }
    }
}

