package videobackend.video;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码验证工具
 * 用于验证数据库中的BCrypt密码是否正确
 */
public class PasswordVerifierTest {

    @Test
    public void verifyPasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的密码哈希
        String storedHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLNO";
        
        // 测试不同的密码
        String[] testPasswords = {"123456", "test", "password", "admin123"};
        
        System.out.println("=== 验证数据库中的密码哈希 ===\n");
        System.out.println("存储的哈希: " + storedHash);
        System.out.println();
        
        boolean found = false;
        for (String password : testPasswords) {
            boolean matches = encoder.matches(password, storedHash);
            System.out.println("测试密码: " + password + " -> " + (matches ? "✓ 匹配" : "✗ 不匹配"));
            if (matches) {
                found = true;
                System.out.println("找到匹配的密码: " + password);
            }
        }
        
        if (!found) {
            System.out.println("\n警告: 数据库中的密码哈希不匹配任何测试密码！");
            System.out.println("需要生成新的BCrypt密码。\n");
        }
        
        // 生成新的123456的BCrypt密码
        System.out.println("\n=== 生成新的BCrypt密码（密码: 123456）===");
        String newHash = encoder.encode("123456");
        System.out.println("新的BCrypt哈希: " + newHash);
        System.out.println("验证新哈希: " + encoder.matches("123456", newHash));
        
        // 生成SQL更新语句
        System.out.println("\n=== SQL更新语句 ===");
        System.out.println("-- 更新test用户的密码为123456");
        System.out.println("UPDATE `users` SET `password` = '" + newHash + "' WHERE `account` = 'test';");
        System.out.println();
        System.out.println("-- 更新newuser用户的密码为123456");
        System.out.println("UPDATE `users` SET `password` = '" + newHash + "' WHERE `account` = 'newuser';");
        System.out.println();
        System.out.println("-- 或者插入新用户");
        System.out.println("INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`)");
        System.out.println("VALUES ('测试用户', 'testuser3', '" + newHash + "', 'testuser3@example.com', 1, NOW(), NOW());");
    }
}

