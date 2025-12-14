-- ============================================
-- 插入新测试用户 - 可直接使用（密码: 123456）
-- ============================================
-- 这些SQL语句可以直接执行，密码已经使用BCrypt加密
-- 所有用户的密码都是: 123456
-- 使用与test用户相同的BCrypt哈希值（因为密码相同）

-- 测试用户1: testuser2 (密码: 123456)
INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
VALUES ('测试用户2', 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLNO', 'testuser2@example.com', 1, NOW(), NOW());

-- 测试用户2: newuser (密码: 123456)
INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
VALUES ('新测试用户', 'newuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLNO', 'newuser@example.com', 1, NOW(), NOW());

-- 测试用户3: admin (密码: 123456)
INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
VALUES ('管理员', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLNO', 'admin@example.com', 1, NOW(), NOW());

-- ============================================
-- 使用说明：
-- 1. 直接在MySQL中执行上面的SQL语句
-- 2. 登录时使用：
--    - 账号: testuser2, 密码: 123456
--    - 账号: newuser, 密码: 123456
--    - 账号: admin, 密码: 123456
-- 3. 如果仍然提示密码错误，请检查后端控制台的密码验证日志
-- ============================================

