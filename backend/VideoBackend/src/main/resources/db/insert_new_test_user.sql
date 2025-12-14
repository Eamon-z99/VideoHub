-- ============================================
-- 插入新测试用户
-- ============================================
-- 使用方法：
-- 1. 运行 PasswordGenerator.java 或 PasswordGeneratorTest.java 生成BCrypt密码
-- 2. 将生成的密码替换下面的 $2a$10$... 部分
-- 3. 执行此SQL语句

-- 测试用户: newuser (密码: 123456)
-- 注意：下面的密码是示例，需要运行工具类生成新的BCrypt密码
INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
VALUES ('新测试用户', 'newuser', '$2a$10$REPLACE_WITH_GENERATED_PASSWORD', 'newuser@example.com', 1, NOW(), NOW());

-- ============================================
-- 快速生成密码的方法：
-- ============================================
-- 方法1: 运行测试类
--   mvn test -Dtest=PasswordGeneratorTest#generatePasswords
--
-- 方法2: 运行main方法（需要先编译）
--   cd backend/VideoBackend
--   mvn compile
--   java -cp "target/classes;target/dependency/*" videobackend.video.util.PasswordGenerator
--
-- 方法3: 在IDE中直接运行 PasswordGenerator.java 的 main 方法
-- ============================================

