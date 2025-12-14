-- 插入新的测试用户
-- 密码: 123456 (BCrypt加密)
-- 注意：BCrypt每次加密结果都不同，所以需要运行PasswordGenerator.java生成新的加密密码

-- 测试用户1: testuser (密码: 123456)
-- 运行 PasswordGenerator.java 生成新的BCrypt密码后，替换下面的密码
INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
VALUES ('测试用户2', 'testuser', '$2a$10$REPLACE_WITH_GENERATED_PASSWORD', 'testuser@example.com', 1, NOW(), NOW());

-- 测试用户2: admin (密码: admin123)
-- INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
-- VALUES ('管理员', 'admin', '$2a$10$REPLACE_WITH_GENERATED_PASSWORD', 'admin@example.com', 1, NOW(), NOW());

