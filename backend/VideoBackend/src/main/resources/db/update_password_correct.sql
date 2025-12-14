-- ============================================
-- 更新密码为正确的BCrypt哈希（密码: 123456）
-- ============================================
-- 问题：当前数据库中的BCrypt哈希可能不是123456的加密结果
-- 解决：运行 VerifyCurrentPassword.java 生成新的BCrypt密码，然后执行下面的UPDATE语句

-- 步骤：
-- 1. 在IDE中运行 VerifyCurrentPassword.java 的 main 方法
-- 2. 如果验证失败，工具会生成新的BCrypt哈希
-- 3. 复制生成的BCrypt哈希（以$2a$10$开头）
-- 4. 替换下面UPDATE语句中的密码哈希
-- 5. 执行SQL语句

-- ============================================
-- 更新test用户的密码为123456
-- ============================================
-- 将下面的 $2a$10$... 替换为 VerifyCurrentPassword.java 生成的哈希值
UPDATE `users` SET `password` = '$2a$10$REPLACE_WITH_GENERATED_HASH' WHERE `account` = 'test';

-- ============================================
-- 更新newuser用户的密码为123456
-- ============================================
-- 将下面的 $2a$10$... 替换为 VerifyCurrentPassword.java 生成的哈希值
UPDATE `users` SET `password` = '$2a$10$REPLACE_WITH_GENERATED_HASH' WHERE `account` = 'newuser';

-- ============================================
-- 验证更新后的密码
-- ============================================
-- 执行更新后，使用以下账号登录测试：
-- 账号: test, 密码: 123456
-- 账号: newuser, 密码: 123456

