-- ============================================
-- 修复密码格式问题
-- ============================================
-- 问题：数据库中的密码缺少BCrypt版本前缀
-- 当前格式: $10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLNO
-- 正确格式: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLNO
-- 
-- BCrypt密码格式说明：
-- $2a$ = BCrypt版本标识符
-- 10$ = 强度/轮数
-- 后面是盐值和哈希值

-- ============================================
-- 方法1: 修复现有密码（添加$2a前缀）
-- ============================================
-- 如果数据库中的密码确实是123456的BCrypt哈希，只是缺少前缀
-- 可以尝试添加$2a前缀（但可能不工作，因为哈希值可能已经损坏）

-- 更新test用户（添加$2a前缀）
UPDATE `users` SET `password` = CONCAT('$2a', `password`) WHERE `account` = 'test' AND `password` LIKE '$10$%';

-- 更新newuser用户（添加$2a前缀）
UPDATE `users` SET `password` = CONCAT('$2a', `password`) WHERE `account` = 'newuser' AND `password` LIKE '$10$%';

-- ============================================
-- 方法2: 重新生成正确的BCrypt密码（推荐）
-- ============================================
-- 如果方法1不工作，需要运行 PasswordGenerator.java 生成新的BCrypt密码
-- 然后执行以下SQL（替换为生成的哈希值）

-- 删除旧用户
-- DELETE FROM `users` WHERE `account` IN ('test', 'newuser');

-- 插入新用户（使用正确的BCrypt密码）
-- INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
-- VALUES 
-- ('测试用户', 'test', '$2a$10$生成的完整哈希值', 'test@example.com', 1, NOW(), NOW()),
-- ('新测试用户', 'newuser', '$2a$10$生成的完整哈希值', 'newuser@example.com', 1, NOW(), NOW());

