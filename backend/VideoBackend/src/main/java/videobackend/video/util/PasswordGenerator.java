package videobackend.video.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成BCrypt加密的密码，方便插入测试用户到数据库
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成几个常用测试密码
        String[] passwords = {"123456", "admin123", "test123", "password"};
        
        System.out.println("=== BCrypt密码生成工具 ===\n");
        
        for (String password : passwords) {
            String encoded = encoder.encode(password);
            System.out.println("明文密码: " + password);
            System.out.println("BCrypt加密: " + encoded);
            System.out.println("验证结果: " + encoder.matches(password, encoded));
            System.out.println("---");
        }
        
        // 生成特定密码的SQL插入语句
        System.out.println("\n=== SQL插入语句示例 ===");
        String testPassword = "123456";
        String encodedPassword = encoder.encode(testPassword);
        System.out.println("-- 插入测试用户（账号: testuser, 密码: 123456）");
        System.out.println("INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) VALUES");
        System.out.println("('测试用户2', 'testuser', '" + encodedPassword + "', 'testuser@example.com', 1, NOW(), NOW());");
        
        // 生成更新密码的SQL语句
        System.out.println("\n=== SQL更新语句（修复现有用户密码）===");
        System.out.println("-- 更新test用户的密码为123456");
        System.out.println("UPDATE `users` SET `password` = '" + encodedPassword + "' WHERE `account` = 'test';");
        System.out.println();
        System.out.println("-- 更新newuser用户的密码为123456");
        System.out.println("UPDATE `users` SET `password` = '" + encodedPassword + "' WHERE `account` = 'newuser';");
        System.out.println();
        
        // 验证生成的密码
        System.out.println("=== 验证生成的密码 ===");
        System.out.println("明文密码: " + testPassword);
        System.out.println("BCrypt哈希: " + encodedPassword);
        System.out.println("验证结果: " + encoder.matches(testPassword, encodedPassword));
    }
}

