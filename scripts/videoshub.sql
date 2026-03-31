/*
 Navicat Premium Data Transfer

 Source Server         : locaohost
 Source Server Type    : MySQL
 Source Server Version : 80040
 Source Host           : localhost:3306
 Source Schema         : videoshub

 Target Server Type    : MySQL
 Target Server Version : 80040
 File Encoding         : 65001

 Date: 31/03/2026 22:06:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admins
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '后台管理员（独立账号）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comment_likes
-- ----------------------------
DROP TABLE IF EXISTS `comment_likes`;
CREATE TABLE `comment_likes`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `comment_id` bigint UNSIGNED NOT NULL COMMENT '评论ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_comment`(`user_id` ASC, `comment_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE,
  CONSTRAINT `comment_likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comment_likes_ibfk_2` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID（回复评论时使用）',
  `reply_to_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '被回复用户ID（仅二次回复）',
  `like_count` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_reply_to_user_id`(`reply_to_user_id` ASC) USING BTREE,
  CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_reply_to_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for fans
-- ----------------------------
DROP TABLE IF EXISTS `fans`;
CREATE TABLE `fans`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `follower_id` bigint UNSIGNED NOT NULL COMMENT '关注者ID',
  `following_id` bigint UNSIGNED NOT NULL COMMENT '被关注者ID',
  `group_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属自定义分组；NULL=未分组',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follower_following`(`follower_id` ASC, `following_id` ASC) USING BTREE,
  INDEX `idx_follower_id`(`follower_id` ASC) USING BTREE,
  INDEX `idx_following_id`(`following_id` ASC) USING BTREE,
  INDEX `idx_follower_group`(`follower_id` ASC, `group_id` ASC) USING BTREE,
  INDEX `fk_fans_follow_group`(`group_id` ASC) USING BTREE,
  CONSTRAINT `fans_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fans_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_fans_follow_group` FOREIGN KEY (`group_id`) REFERENCES `follow_group` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fans_chk_1` CHECK (`follower_id` <> `following_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for favorite_folders
-- ----------------------------
DROP TABLE IF EXISTS `favorite_folders`;
CREATE TABLE `favorite_folders`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收藏夹名称',
  `is_public` tinyint NULL DEFAULT 1 COMMENT '是否公开：0-私密，1-公开',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收藏夹描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_name`(`user_id` ASC, `name` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `favorite_folders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6327 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏夹表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `folder_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '收藏夹ID（为空表示默认收藏夹）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_folder_id`(`folder_id` ASC) USING BTREE,
  INDEX `idx_favorites_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_favorites_video_user`(`video_id` ASC, `user_id` ASC) USING BTREE,
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `favorites_ibfk_3` FOREIGN KEY (`folder_id`) REFERENCES `favorite_folders` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 232992 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for feed_likes
-- ----------------------------
DROP TABLE IF EXISTS `feed_likes`;
CREATE TABLE `feed_likes`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `feed_id` bigint UNSIGNED NOT NULL COMMENT '动态ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_feed_user`(`feed_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_feed_id`(`feed_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `feed_likes_ibfk_1` FOREIGN KEY (`feed_id`) REFERENCES `feeds` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `feed_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for feeds
-- ----------------------------
DROP TABLE IF EXISTS `feeds`;
CREATE TABLE `feeds`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '发布用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '动态标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片URL列表（JSON格式）',
  `like_count` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int UNSIGNED NULL DEFAULT 0 COMMENT '评论数',
  `share_count` int UNSIGNED NULL DEFAULT 0 COMMENT '转发数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `feeds_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for follow_group
-- ----------------------------
DROP TABLE IF EXISTS `follow_group`;
CREATE TABLE `follow_group`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '分组所有者（即关注方，对应 fans.follower_id）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组名称',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_group_name`(`user_id` ASC, `name` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_sort`(`user_id` ASC, `sort_order` ASC) USING BTREE,
  CONSTRAINT `follow_group_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注分组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for home_hero_carousel
-- ----------------------------
DROP TABLE IF EXISTS `home_hero_carousel`;
CREATE TABLE `home_hero_carousel`  (
  `slot` int NOT NULL,
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`slot`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for play_history
-- ----------------------------
DROP TABLE IF EXISTS `play_history`;
CREATE TABLE `play_history`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `play_time` int UNSIGNED NULL DEFAULT 0 COMMENT '播放到的时间点（秒）',
  `duration` int UNSIGNED NULL DEFAULT NULL COMMENT '视频总时长（秒）',
  `is_watched` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否观看完成（1=完成，0=未完成）',
  `watch_count` int UNSIGNED NOT NULL DEFAULT 1 COMMENT '观看次数',
  `last_watch_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间',
  `progress_percent` decimal(5, 2) UNSIGNED NULL DEFAULT 0.00 COMMENT '观看进度百分比（0-100）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_last_watch`(`user_id` ASC, `last_watch_time` DESC) USING BTREE,
  INDEX `idx_user_watched`(`user_id` ASC, `is_watched` ASC) USING BTREE,
  INDEX `idx_play_history_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_play_history_video_user`(`video_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_play_history_video_play_time`(`video_id` ASC, `play_time` ASC) USING BTREE,
  CONSTRAINT `play_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `play_history_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1023199 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '播放历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for private_messages
-- ----------------------------
DROP TABLE IF EXISTS `private_messages`;
CREATE TABLE `private_messages`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '私信ID',
  `sender_id` bigint UNSIGNED NOT NULL COMMENT '发送方用户ID',
  `receiver_id` bigint UNSIGNED NOT NULL COMMENT '接收方用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver_time`(`sender_id` ASC, `receiver_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_receiver_read_time`(`receiver_id` ASC, `is_read` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `private_messages_ibfk_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `private_messages_ibfk_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户私信表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for profile_visits
-- ----------------------------
DROP TABLE IF EXISTS `profile_visits`;
CREATE TABLE `profile_visits`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '访问记录ID',
  `visitor_id` bigint UNSIGNED NOT NULL COMMENT '访问者用户ID',
  `profile_user_id` bigint UNSIGNED NOT NULL COMMENT '被访问主页用户ID',
  `visit_date` date NOT NULL COMMENT '访问日期（按日去重）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次访问时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_visitor_profile_date`(`visitor_id` ASC, `profile_user_id` ASC, `visit_date` ASC) USING BTREE,
  INDEX `idx_profile_date`(`profile_user_id` ASC, `visit_date` ASC) USING BTREE,
  INDEX `idx_visitor`(`visitor_id` ASC) USING BTREE,
  CONSTRAINT `fk_profile_visits_profile_user` FOREIGN KEY (`profile_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_profile_visits_visitor` FOREIGN KEY (`visitor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '个人主页访问记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for search_hot_keywords
-- ----------------------------
DROP TABLE IF EXISTS `search_hot_keywords`;
CREATE TABLE `search_hot_keywords`  (
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_seen_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_seen_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `score` double NOT NULL DEFAULT 0,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyword`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for search_keyword_events
-- ----------------------------
DROP TABLE IF EXISTS `search_keyword_events`;
CREATE TABLE `search_keyword_events`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NULL DEFAULT NULL,
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `weight` int UNSIGNED NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_keyword_time`(`keyword` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `search_keyword_events_ibfk_1`(`user_id` ASC) USING BTREE,
  CONSTRAINT `search_keyword_events_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for transcode_tasks
-- ----------------------------
DROP TABLE IF EXISTS `transcode_tasks`;
CREATE TABLE `transcode_tasks`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `source_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源文件路径',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-等待中，PROCESSING-处理中，DONE-完成，FAILED-失败',
  `progress` tinyint UNSIGNED NULL DEFAULT 0 COMMENT '进度（0-100）',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '转码任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_daily_login_coin_grants
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_login_coin_grants`;
CREATE TABLE `user_daily_login_coin_grants`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '发放记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `grant_date` date NOT NULL COMMENT '发放日期（按日）',
  `coin_granted` int UNSIGNED NOT NULL DEFAULT 1 COMMENT '发放硬币数（当前固定 1）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `grant_date` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_grant_date`(`grant_date` ASC) USING BTREE,
  CONSTRAINT `user_daily_login_coin_grants_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户每日登录硬币奖励发放记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_exp_daily_grants
-- ----------------------------
DROP TABLE IF EXISTS `user_exp_daily_grants`;
CREATE TABLE `user_exp_daily_grants`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '发放记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `grant_date` date NOT NULL COMMENT '发放日期（按日）',
  `exp_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经验类型：LOGIN/WATCH/COIN_GIFT',
  `exp_granted` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '当日累计发放经验值',
  `coin_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '当日累计投币次数（仅 COIN_GIFT 使用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date_type`(`user_id` ASC, `grant_date` ASC, `exp_type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_grant_date`(`grant_date` ASC) USING BTREE,
  CONSTRAINT `user_exp_daily_grants_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户每日经验发放记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密后）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `coin_balance` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户硬币余额',
  `level` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户等级（0-6，预留更高等级）',
  `exp` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前经验值（用于计算等级）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `account`(`account` ASC) USING BTREE,
  INDEX `idx_account`(`account` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6347 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for avatar_submissions
-- ----------------------------
DROP TABLE IF EXISTS `avatar_submissions`;
CREATE TABLE `avatar_submissions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '头像审核记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '提交用户ID',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '待审头像URL（本地 /avatars/** 或 CDN URL）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING-待审,APPROVED-通过,REJECTED-驳回,REPLACED-被新提交替换',
  `reviewer_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '审核管理员ID(admins.id)',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注/驳回原因',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_avatar_submissions_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_avatar_submissions_status`(`status` ASC) USING BTREE,
  INDEX `idx_avatar_submissions_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_avatar_submissions_reviewer`(`reviewer_id` ASC) USING BTREE,
  CONSTRAINT `fk_avatar_submissions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_avatar_submissions_admin` FOREIGN KEY (`reviewer_id`) REFERENCES `admins` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户头像提交/审核表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_coins
-- ----------------------------
DROP TABLE IF EXISTS `video_coins`;
CREATE TABLE `video_coins`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '投币ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '投币用户ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `video_coins_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `video_coins_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频投币表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_collection_subscriptions
-- ----------------------------
DROP TABLE IF EXISTS `video_collection_subscriptions`;
CREATE TABLE `video_collection_subscriptions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '订阅用户',
  `collection_id` bigint UNSIGNED NOT NULL COMMENT '投稿合集ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_collection`(`user_id` ASC, `collection_id` ASC) USING BTREE,
  INDEX `idx_sub_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_sub_collection`(`collection_id` ASC) USING BTREE,
  CONSTRAINT `fk_vcs_collection` FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_vcs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户订阅投稿合集' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_collections
-- ----------------------------
DROP TABLE IF EXISTS `video_collections`;
CREATE TABLE `video_collections`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '合集ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '创建者用户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合集名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序（小在前）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_collections_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_video_collections_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'UP主投稿合集' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_complaints
-- ----------------------------
DROP TABLE IF EXISTS `video_complaints`;
CREATE TABLE `video_complaints`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `reporter_user_id` bigint UNSIGNED NOT NULL COMMENT '举报用户ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报类型标识',
  `other_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '其他说明',
  `evidence_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '证据图片URL(JSON数组字符串)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0-待处理,1-已受理,2-不予受理',
  `handler_admin_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '处理管理员ID',
  `handler_action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理动作：WARN/TAKEDOWN/DISMISS',
  `handler_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_reporter_user_id`(`reporter_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_handler_admin_id`(`handler_admin_id` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  CONSTRAINT `fk_vc_handler_admin` FOREIGN KEY (`handler_admin_id`) REFERENCES `admins` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_vc_reporter` FOREIGN KEY (`reporter_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_vc_video` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频举报表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_drafts
-- ----------------------------
DROP TABLE IF EXISTS `video_drafts`;
CREATE TABLE `video_drafts`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '草稿ID',
  `submission_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对应 video_submissions.submission_id',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '简介',
  `copyright` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'original' COMMENT '类型：original-自制，repost-转载',
  `partition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分区',
  `tags` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '[]' COMMENT '标签（JSON数组字符串）',
  `duration` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '时长（秒）',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面相对路径/URL',
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频存储相对路径',
  `source_file` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始文件路径',
  `file_size` bigint UNSIGNED NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `schedule_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否定时发布：0-否，1-是',
  `schedule_publish_at` datetime NULL DEFAULT NULL COMMENT '定时发布时间',
  `collection_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否加入合集：0-否，1-是',
  `collection_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合集名称',
  `collection_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '投稿选择的合集ID',
  `allow_second_creation` tinyint(1) NULL DEFAULT 0 COMMENT '允许二创：0-否，1-是',
  `commercial_promotion` tinyint(1) NULL DEFAULT 0 COMMENT '商业推广信息：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE,
  INDEX `idx_video_drafts_collection_id`(`collection_id` ASC) USING BTREE,
  CONSTRAINT `video_drafts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_video_drafts_video_collection` FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频草稿表（内容管理-草稿箱）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_likes
-- ----------------------------
DROP TABLE IF EXISTS `video_likes`;
CREATE TABLE `video_likes`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_video_likes_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_video_likes_video_user`(`video_id` ASC, `user_id` ASC) USING BTREE,
  CONSTRAINT `video_likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `video_likes_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 584315 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_notes
-- ----------------------------
DROP TABLE IF EXISTS `video_notes`;
CREATE TABLE `video_notes`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `author_user_id` bigint UNSIGNED NOT NULL COMMENT '笔记作者用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容（文本/富文本）',
  `visibility` tinyint NOT NULL DEFAULT 1 COMMENT '1-公开，0-仅自己可见',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0-删除，1-正常',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_author_user_id`(`author_user_id` ASC) USING BTREE,
  INDEX `idx_visibility`(`visibility` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  CONSTRAINT `fk_vn_author` FOREIGN KEY (`author_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_vn_video` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频笔记表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_play_events
-- ----------------------------
DROP TABLE IF EXISTS `video_play_events`;
CREATE TABLE `video_play_events`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '播放事件ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '观看用户ID，未登录可为空',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID',
  `creator_id` bigint UNSIGNED NOT NULL COMMENT '视频作者ID（冗余，便于聚合）',
  `played_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '播放发生时间',
  `play_seconds` int UNSIGNED NULL DEFAULT 0 COMMENT '本次播放时长（可选）',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID（可选，防刷/去重）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator_played_at`(`creator_id` ASC, `played_at` ASC) USING BTREE,
  INDEX `idx_video_played_at`(`video_id` ASC, `played_at` ASC) USING BTREE,
  INDEX `idx_user_played_at`(`user_id` ASC, `played_at` ASC) USING BTREE,
  CONSTRAINT `fk_vpe_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_vpe_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_vpe_video` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 189 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频播放事件流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video_submissions
-- ----------------------------
DROP TABLE IF EXISTS `video_submissions`;
CREATE TABLE `video_submissions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '投稿ID',
  `submission_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '投稿唯一标识（如 sub_1_时间戳）',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '投稿用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '简介',
  `copyright` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'original' COMMENT 'original-自制，repost-转载',
  `partition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分区',
  `tags` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '[]' COMMENT '标签（JSON数组字符串）',
  `duration` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '时长（秒）',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面相对路径/URL',
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频存储相对路径',
  `source_file` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始文件路径',
  `file_size` bigint UNSIGNED NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `schedule_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否定时发布',
  `schedule_publish_at` datetime NULL DEFAULT NULL COMMENT '定时发布时间',
  `collection_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否加入合集',
  `collection_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合集名称',
  `collection_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '投稿选择的合集ID',
  `allow_second_creation` tinyint(1) NULL DEFAULT 0 COMMENT '允许二创',
  `commercial_promotion` tinyint(1) NULL DEFAULT 0 COMMENT '商业推广信息',
  `review_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING-待审, APPROVED-通过, REJECTED-驳回',
  `reviewer_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '审核人ID(管理端)',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注/驳回原因',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `published_video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核通过后写入videos的video_id',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '实际发布时间（通过后/到点发布）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_review_status`(`review_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_schedule_publish_at`(`schedule_publish_at` ASC) USING BTREE,
  INDEX `idx_published_video_id`(`published_video_id` ASC) USING BTREE,
  INDEX `idx_video_submissions_collection_id`(`collection_id` ASC) USING BTREE,
  CONSTRAINT `fk_video_submissions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_video_submissions_video_collection` FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频投稿/审核表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for videos
-- ----------------------------
DROP TABLE IF EXISTS `videos`;
CREATE TABLE `videos`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `video_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频唯一标识（如video_0001）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '视频描述',
  `duration` int UNSIGNED NOT NULL COMMENT '时长（秒）',
  `width` int UNSIGNED NULL DEFAULT NULL COMMENT '视频宽度',
  `height` int UNSIGNED NULL DEFAULT NULL COMMENT '视频高度',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储路径（相对路径）',
  `source_file` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始文件路径',
  `file_size` bigint UNSIGNED NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PROCESSING' COMMENT '状态：PROCESSING-转码中，DONE-已完成，FAILED-失败',
  `view_count` bigint UNSIGNED NULL DEFAULT 0 COMMENT '播放次数',
  `like_count` bigint UNSIGNED NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` bigint UNSIGNED NULL DEFAULT 0 COMMENT '收藏数',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '上传用户ID',
  `collection_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '投稿合集ID，NULL 表示未加入合集',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `partition` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分区导航（JSON 数组，对应首页六个快捷入口）',
  `tags` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '[]' COMMENT '标签（JSON数组字符串）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `video_id`(`video_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_videos_collection_id`(`collection_id` ASC) USING BTREE,
  CONSTRAINT `videos_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_videos_video_collection` FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6681 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for watch_later
-- ----------------------------
DROP TABLE IF EXISTS `watch_later`;
CREATE TABLE `watch_later`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `video_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频ID，与 videos.video_id 一致',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_watch_later_user_video`(`user_id` ASC, `video_id` ASC) USING BTREE,
  INDEX `idx_watch_later_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '稍后再看' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
