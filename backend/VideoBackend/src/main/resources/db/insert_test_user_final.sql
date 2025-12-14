-- ============================================
-- 插入新测试用户 - 可直接使用
-- ============================================
-- 这些SQL语句可以直接执行，密码已经使用BCrypt加密
-- 密码都是: 123456

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
-- 说明：
-- 1. 所有用户的密码都是 123456
-- 2. 密码使用BCrypt加密，与test用户使用相同的哈希值
-- 3. 可以直接在MySQL中执行这些SQL语句
-- 4. 如果需要生成新的密码，请运行 PasswordGenerator.java
-- ============================================

