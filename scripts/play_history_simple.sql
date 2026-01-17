-- ============================================
-- 播放历史表增强 - 简化版SQL
-- ============================================
-- 适用于：已有play_history表，需要添加新字段
-- 执行前请备份数据库！
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 添加新字段（如果字段已存在会报错，可以忽略）
ALTER TABLE `play_history` 
ADD COLUMN `is_watched` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否观看完成（1=完成，0=未完成）' AFTER `duration`,
ADD COLUMN `watch_count` int UNSIGNED NOT NULL DEFAULT 1 COMMENT '观看次数' AFTER `is_watched`,
ADD COLUMN `last_watch_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间' AFTER `watch_count`,
ADD COLUMN `progress_percent` decimal(5,2) UNSIGNED NULL DEFAULT 0.00 COMMENT '观看进度百分比（0-100）' AFTER `last_watch_time`;

-- 添加索引
ALTER TABLE `play_history` 
ADD INDEX `idx_user_last_watch` (`user_id`, `last_watch_time` DESC),
ADD INDEX `idx_user_watched` (`user_id`, `is_watched`);

-- 更新现有数据
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

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 创建触发器（可选，用于自动维护字段）
-- ============================================

DROP TRIGGER IF EXISTS `trg_play_history_update_progress`;

DELIMITER $$

CREATE TRIGGER `trg_play_history_update_progress`
BEFORE UPDATE ON `play_history`
FOR EACH ROW
BEGIN
    IF NEW.duration IS NOT NULL AND NEW.duration > 0 THEN
        SET NEW.progress_percent = LEAST(100.00, (NEW.play_time * 100.0 / NEW.duration));
        IF NEW.progress_percent >= 90.00 THEN
            SET NEW.is_watched = 1;
        ELSE
            SET NEW.is_watched = 0;
        END IF;
    ELSE
        SET NEW.progress_percent = 0.00;
        SET NEW.is_watched = 0;
    END IF;
    SET NEW.last_watch_time = NOW();
END$$

DELIMITER ;

DROP TRIGGER IF EXISTS `trg_play_history_insert_progress`;

DELIMITER $$

CREATE TRIGGER `trg_play_history_insert_progress`
BEFORE INSERT ON `play_history`
FOR EACH ROW
BEGIN
    IF NEW.duration IS NOT NULL AND NEW.duration > 0 THEN
        SET NEW.progress_percent = LEAST(100.00, (NEW.play_time * 100.0 / NEW.duration));
        IF NEW.progress_percent >= 90.00 THEN
            SET NEW.is_watched = 1;
        ELSE
            SET NEW.is_watched = 0;
        END IF;
    ELSE
        SET NEW.progress_percent = 0.00;
        SET NEW.is_watched = 0;
    END IF;
    IF NEW.last_watch_time IS NULL THEN
        SET NEW.last_watch_time = NOW();
    END IF;
END$$

DELIMITER ;

