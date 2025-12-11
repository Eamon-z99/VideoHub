-- ============================================
-- VideoHub 数据库创建脚本
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS videohub 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE videohub;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `account` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_account` (`account`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 视频表
-- ============================================
CREATE TABLE IF NOT EXISTS `videos` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '视频ID',
    `video_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '视频唯一标识（如video_0001）',
    `title` VARCHAR(200) NOT NULL COMMENT '视频标题',
    `description` TEXT DEFAULT NULL COMMENT '视频描述',
    `duration` INT UNSIGNED NOT NULL COMMENT '时长（秒）',
    `width` INT UNSIGNED DEFAULT NULL COMMENT '视频宽度',
    `height` INT UNSIGNED DEFAULT NULL COMMENT '视频高度',
    `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径（相对路径）',
    `source_file` VARCHAR(500) DEFAULT NULL COMMENT '原始文件路径',
    `file_size` BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小（字节）',
    `status` VARCHAR(20) DEFAULT 'PROCESSING' COMMENT '状态：PROCESSING-转码中，DONE-已完成，FAILED-失败',
    `view_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '播放次数',
    `like_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `favorite_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '收藏数',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_video_id` (`video_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频表';

-- ============================================
-- 3. 转码任务表
-- ============================================
CREATE TABLE IF NOT EXISTS `transcode_tasks` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `video_id` VARCHAR(50) NOT NULL COMMENT '视频ID',
    `source_path` VARCHAR(500) NOT NULL COMMENT '源文件路径',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-等待中，PROCESSING-处理中，DONE-完成，FAILED-失败',
    `progress` TINYINT UNSIGNED DEFAULT 0 COMMENT '进度（0-100）',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_video_id` (`video_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转码任务表';

-- ============================================
-- 4. 播放历史表
-- ============================================
CREATE TABLE IF NOT EXISTS `play_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `video_id` VARCHAR(50) NOT NULL COMMENT '视频ID',
    `play_time` INT UNSIGNED DEFAULT 0 COMMENT '播放到的时间点（秒）',
    `duration` INT UNSIGNED DEFAULT NULL COMMENT '视频总时长（秒）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_video_id` (`video_id`),
    INDEX `idx_create_time` (`create_time`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='播放历史表';

-- ============================================
-- 5. 收藏表
-- ============================================
CREATE TABLE IF NOT EXISTS `favorites` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `video_id` VARCHAR(50) NOT NULL COMMENT '视频ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_video_id` (`video_id`),
    INDEX `idx_create_time` (`create_time`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================
-- 6. 评论表
-- ============================================
CREATE TABLE IF NOT EXISTS `comments` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `video_id` VARCHAR(50) NOT NULL COMMENT '视频ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（回复评论时使用）',
    `like_count` INT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_video_id` (`video_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_create_time` (`create_time`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 7. 点赞表（视频点赞）
-- ============================================
CREATE TABLE IF NOT EXISTS `video_likes` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `video_id` VARCHAR(50) NOT NULL COMMENT '视频ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_video_id` (`video_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频点赞表';

-- ============================================
-- 8. 评论点赞表
-- ============================================
CREATE TABLE IF NOT EXISTS `comment_likes` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `comment_id` BIGINT UNSIGNED NOT NULL COMMENT '评论ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_comment_id` (`comment_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- ============================================
-- 9. 关注表
-- ============================================
CREATE TABLE IF NOT EXISTS `follows` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关注ID',
    `follower_id` BIGINT UNSIGNED NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    INDEX `idx_follower_id` (`follower_id`),
    INDEX `idx_following_id` (`following_id`),
    FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CHECK (`follower_id` != `following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- ============================================
-- 初始化数据（可选）
-- ============================================

-- 插入测试用户（密码：123456，使用BCrypt加密后的值，实际使用时需要替换）
INSERT INTO `users` (`username`, `account`, `password`, `email`) VALUES
('测试用户', 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLNO', 'test@example.com');







