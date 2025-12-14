-- ============================================
-- 修复密码问题 - 更新用户密码为123456
-- ============================================
-- 问题：数据库中的BCrypt密码哈希可能不正确
-- 解决：运行 PasswordVerifierTest.java 生成新的BCrypt密码，然后执行下面的UPDATE语句

-- 注意：下面的密码哈希是示例，需要运行测试类生成新的
-- 运行命令: mvn test -Dtest=PasswordVerifierTest#verifyPasswords

-- 更新test用户的密码为123456
-- UPDATE `users` SET `password` = '$2a$10$REPLACE_WITH_NEW_HASH' WHERE `account` = 'test';

-- 更新newuser用户的密码为123456
-- UPDATE `users` SET `password` = '$2a$10$REPLACE_WITH_NEW_HASH' WHERE `account` = 'newuser';

-- ============================================
-- 临时解决方案：直接生成新的密码哈希
-- ============================================
-- 如果无法运行测试类，可以使用以下方法：
-- 1. 在IDE中运行 PasswordGenerator.java 的 main 方法
-- 2. 复制生成的BCrypt哈希
-- 3. 替换下面的 $2a$10$... 部分

-- 示例（需要替换为实际生成的哈希）:
-- UPDATE `users` SET `password` = '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx' WHERE `account` = 'test';

