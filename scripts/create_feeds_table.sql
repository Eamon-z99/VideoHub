-- ============================================
-- 动态表（feeds）创建脚本
-- 用于存储用户发布的动态（非视频内容）
-- ============================================

USE videohub;

-- 创建动态表
DROP TABLE IF EXISTS `feeds`;
CREATE TABLE `feeds` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '发布用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '动态标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片URL列表（JSON格式）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态表' ROW_FORMAT = Dynamic;

-- 创建动态点赞表
DROP TABLE IF EXISTS `feed_likes`;
CREATE TABLE `feed_likes` (
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态点赞表' ROW_FORMAT = Dynamic;

