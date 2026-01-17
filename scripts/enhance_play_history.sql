-- ============================================
-- 增强版播放历史表 (play_history) SQL语句
-- ============================================
-- 说明：此SQL用于增强现有的play_history表，添加更多字段以支持完整的历史记录功能
-- 包括：观看进度、是否完成、观看次数等
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 方案一：如果表已存在，使用ALTER TABLE添加新字段
-- ============================================
-- 注意：MySQL 8.0.19+ 支持 IF NOT EXISTS，旧版本需要手动检查
-- 如果执行报错"Duplicate column name"，说明字段已存在，可以忽略

-- 添加是否观看完成字段（观看进度>=90%视为完成）
-- MySQL 8.0.19+ 可以使用 IF NOT EXISTS
ALTER TABLE `play_history` 
ADD COLUMN `is_watched` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否观看完成（1=完成，0=未完成）' AFTER `duration`;

-- 添加观看次数字段
ALTER TABLE `play_history` 
ADD COLUMN `watch_count` int UNSIGNED NOT NULL DEFAULT 1 COMMENT '观看次数' AFTER `is_watched`;

-- 添加最后观看时间字段（用于排序，与update_time类似但更明确）
ALTER TABLE `play_history` 
ADD COLUMN `last_watch_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间' AFTER `watch_count`;

-- 添加观看进度百分比字段（冗余字段，便于查询，可通过play_time/duration计算）
ALTER TABLE `play_history` 
ADD COLUMN `progress_percent` decimal(5,2) UNSIGNED NULL DEFAULT 0.00 COMMENT '观看进度百分比（0-100）' AFTER `last_watch_time`;

-- 添加索引以优化查询性能
-- 按用户ID和最后观看时间排序（用于历史记录列表）
-- 注意：如果索引已存在会报错，可以忽略
ALTER TABLE `play_history` 
ADD INDEX `idx_user_last_watch` (`user_id`, `last_watch_time` DESC);

-- 按用户ID和是否观看完成查询
ALTER TABLE `play_history` 
ADD INDEX `idx_user_watched` (`user_id`, `is_watched`);

-- ============================================
-- 方案二：如果表不存在，创建完整的表结构（推荐用于新环境）
-- ============================================

DROP TABLE IF EXISTS `play_history`;
CREATE TABLE `play_history` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `play_time` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '播放到的时间点（秒）',
  `duration` int UNSIGNED NULL DEFAULT NULL COMMENT '视频总时长（秒）',
  `is_watched` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否观看完成（1=完成，0=未完成，观看进度>=90%视为完成）',
  `watch_count` int UNSIGNED NOT NULL DEFAULT 1 COMMENT '观看次数',
  `last_watch_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间',
  `progress_percent` decimal(5,2) UNSIGNED NULL DEFAULT 0.00 COMMENT '观看进度百分比（0-100）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（首次观看时间）',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video` (`user_id`, `video_id`) USING BTREE COMMENT '用户-视频唯一索引',
  INDEX `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_video_id` (`video_id`) USING BTREE COMMENT '视频ID索引',
  INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引',
  INDEX `idx_user_last_watch` (`user_id`, `last_watch_time` DESC) USING BTREE COMMENT '用户最后观看时间索引（用于历史记录列表排序）',
  INDEX `idx_user_watched` (`user_id`, `is_watched`) USING BTREE COMMENT '用户观看完成状态索引',
  CONSTRAINT `play_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `play_history_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '播放历史表' ROW_FORMAT = Dynamic;

-- ============================================
-- 触发器：自动更新观看进度百分比和是否观看完成状态
-- ============================================

-- 删除已存在的触发器
DROP TRIGGER IF EXISTS `trg_play_history_update_progress`;

-- 创建触发器：当play_time或duration更新时，自动计算progress_percent和is_watched
DELIMITER $$

CREATE TRIGGER `trg_play_history_update_progress`
BEFORE UPDATE ON `play_history`
FOR EACH ROW
BEGIN
    -- 计算观看进度百分比
    IF NEW.duration IS NOT NULL AND NEW.duration > 0 THEN
        SET NEW.progress_percent = LEAST(100.00, (NEW.play_time * 100.0 / NEW.duration));
        
        -- 如果观看进度>=90%，视为观看完成
        IF NEW.progress_percent >= 90.00 THEN
            SET NEW.is_watched = 1;
        ELSE
            SET NEW.is_watched = 0;
        END IF;
    ELSE
        SET NEW.progress_percent = 0.00;
        SET NEW.is_watched = 0;
    END IF;
    
    -- 更新最后观看时间
    SET NEW.last_watch_time = NOW();
END$$

DELIMITER ;

-- 创建插入触发器：插入时也自动计算
DROP TRIGGER IF EXISTS `trg_play_history_insert_progress`;

DELIMITER $$

CREATE TRIGGER `trg_play_history_insert_progress`
BEFORE INSERT ON `play_history`
FOR EACH ROW
BEGIN
    -- 计算观看进度百分比
    IF NEW.duration IS NOT NULL AND NEW.duration > 0 THEN
        SET NEW.progress_percent = LEAST(100.00, (NEW.play_time * 100.0 / NEW.duration));
        
        -- 如果观看进度>=90%，视为观看完成
        IF NEW.progress_percent >= 90.00 THEN
            SET NEW.is_watched = 1;
        ELSE
            SET NEW.is_watched = 0;
        END IF;
    ELSE
        SET NEW.progress_percent = 0.00;
        SET NEW.is_watched = 0;
    END IF;
    
    -- 设置最后观看时间
    IF NEW.last_watch_time IS NULL THEN
        SET NEW.last_watch_time = NOW();
    END IF;
END$$

DELIMITER ;

-- ============================================
-- 数据迁移脚本：为现有数据更新新字段
-- ============================================

-- 更新现有记录的progress_percent和is_watched
UPDATE `play_history` 
SET 
    `progress_percent` = CASE 
        WHEN `duration` IS NOT NULL AND `duration` > 0 
        THEN LEAST(100.00, (`play_time` * 100.0 / `duration`))
        ELSE 0.00
    END,
    `is_watched` = CASE 
        WHEN `duration` IS NOT NULL AND `duration` > 0 AND (`play_time` * 100.0 / `duration`) >= 90.00
        THEN 1
        ELSE 0
    END,
    `last_watch_time` = COALESCE(`update_time`, `create_time`, NOW())
WHERE `progress_percent` = 0.00 OR `progress_percent` IS NULL;

-- 更新现有记录的watch_count（如果有多条相同user_id和video_id的记录，合并为一条并累加次数）
-- 注意：由于有UNIQUE索引uk_user_video，理论上不应该有重复记录
-- 但如果有历史数据需要清理，可以使用以下SQL：
-- DELETE ph1 FROM play_history ph1
-- INNER JOIN play_history ph2 
-- WHERE ph1.id > ph2.id 
-- AND ph1.user_id = ph2.user_id 
-- AND ph1.video_id = ph2.video_id;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 使用说明：
-- ============================================
-- 1. 如果表已存在，请使用方案一（ALTER TABLE）添加新字段
-- 2. 如果是新环境，可以直接使用方案二（DROP TABLE + CREATE TABLE）
-- 3. 触发器会自动维护progress_percent和is_watched字段
-- 4. 建议在低峰期执行数据迁移脚本
-- ============================================

