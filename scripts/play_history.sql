/*
 Navicat Premium Data Transfer

 Source Server         : locaohost
 Source Server Type    : MySQL
 Source Server Version : 80040
 Source Host           : localhost:3306
 Source Schema         : videohub

 Target Server Type    : MySQL
 Target Server Version : 80040
 File Encoding         : 65001

 Date: 18/01/2026 00:54:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
  CONSTRAINT `play_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `play_history_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '播放历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of play_history
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
