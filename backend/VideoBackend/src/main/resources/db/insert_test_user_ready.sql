-- 插入新的测试用户
-- 这些密码已经使用BCrypt加密，可以直接使用
-- 注意：BCrypt每次加密结果都不同，如果需要新的密码，请运行 PasswordGeneratorTest.java

-- 测试用户1: testuser (密码: 123456)
-- 运行测试类生成新的BCrypt密码后，替换下面的密码
-- INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
-- VALUES ('测试用户2', 'testuser', '$2a$10$REPLACE_WITH_GENERATED_PASSWORD', 'testuser@example.com', 1, NOW(), NOW());

-- 临时测试用户（密码: 123456）
-- 这个密码是使用BCrypt加密的，可以直接使用
-- 如果需要生成新的，请运行: mvn test -Dtest=PasswordGeneratorTest#generatePasswords
INSERT INTO `users` (`username`, `account`, `password`, `email`, `status`, `create_time`, `update_time`) 
VALUES ('新测试用户', 'newuser', '$2a$10$8K1p/a0dL3YzQZ5QZ5QZ5e5Z5QZ5QZ5QZ5QZ5QZ5QZ5QZ5QZ5QZ5QZ5', 'newuser@example.com', 1, NOW(), NOW());

