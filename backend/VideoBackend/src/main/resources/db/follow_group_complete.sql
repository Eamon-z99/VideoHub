-- ============================================================
-- 关注分组：推荐执行的完整脚本（MySQL 8.0+）
-- 1）新建 follow_group 表
-- 2）为已有 fans 表增加 group_id 及外键
-- 若你的库是由本项目 schema.sql 一次性初始化且已含本结构，无需再执行。
-- ============================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `follow_group` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '分组所有者（即关注方，对应 fans.follower_id）',
    `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_group_name` (`user_id`, `name`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_sort` (`user_id`, `sort_order`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注分组表';

ALTER TABLE `fans`
    ADD COLUMN `group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属自定义分组；NULL=未分组' AFTER `following_id`;

ALTER TABLE `fans`
    ADD INDEX `idx_follower_group` (`follower_id`, `group_id`);

ALTER TABLE `fans`
    ADD CONSTRAINT `fk_fans_follow_group` FOREIGN KEY (`group_id`) REFERENCES `follow_group` (`id`) ON DELETE SET NULL;
