/*
 * 与 videohubc.sql 配套：独立 admins 表（不关联 users）
 *
 * 若库里已有旧版 admins（含 user_id 外键），请先备份数据后执行：
 *   DROP TABLE IF EXISTS admins;
 * 再执行本脚本。
 */

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `admins`;

CREATE TABLE `admins` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt）',
  `display_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示名',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admins_account`(`account` ASC) USING BTREE,
  INDEX `idx_admins_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '后台管理员（独立账号）' ROW_FORMAT = DYNAMIC;

-- 默认账号 admin；哈希与 videohub 种子用户相同（生产请改密码）
INSERT INTO `admins` (`account`, `password`, `display_name`, `remark`, `status`)
VALUES (
  'admin',
  '$2a$10$/UbxnmyC1Gh7X0K.S90DgOZerW8.gUP2jHXCEyMRwu4Qe0dWHvMzO',
  '系统管理员',
  '默认',
  1
);
