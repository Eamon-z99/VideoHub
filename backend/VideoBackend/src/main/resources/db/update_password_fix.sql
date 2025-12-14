-- ============================================
-- 修复密码 - 更新现有用户的密码为123456
-- ============================================
-- 问题：数据库中的BCrypt密码哈希不正确，导致验证失败
-- 解决：运行 PasswordGenerator.java 生成新的BCrypt密码，然后执行下面的UPDATE语句

-- 步骤：
-- 1. 在IDE中运行 PasswordGenerator.java 的 main 方法
-- 2. 复制生成的BCrypt哈希（以$2a$10$开头）
-- 3. 替换下面UPDATE语句中的密码哈希
-- 4. 执行SQL语句

-- ============================================
-- 更新test用户的密码为123456
-- ============================================
-- 将下面的 $2a$10$... 替换为 PasswordGenerator.java 生成的哈希值
UPDATE `users` SET `password` = '$2a$10$REPLACE_WITH_GENERATED_HASH' WHERE `account` = 'test';

-- ============================================
-- 更新newuser用户的密码为123456
-- ============================================
-- 将下面的 $2a$10$... 替换为 PasswordGenerator.java 生成的哈希值
UPDATE `users` SET `password` = '$2a$10$REPLACE_WITH_GENERATED_HASH' WHERE `account` = 'newuser';

-- ============================================
-- 或者，删除旧用户，插入新用户
-- ============================================
-- DELETE FROM `users` WHERE `account` IN ('test', 'newuser');
-- 
-- INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
-- VALUES 
-- ('测试用户', 'test', '$2a$10$REPLACE_WITH_GENERATED_HASH', 'test@example.com', 1, NOW(), NOW()),
-- ('新测试用户', 'newuser', '$2a$10$REPLACE_WITH_GENERATED_HASH', 'newuser@example.com', 1, NOW(), NOW());

