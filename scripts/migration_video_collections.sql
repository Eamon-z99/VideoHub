-- 视频「投稿合集」增量迁移（在现有 videoshub 库执行）
-- 合集归属 UP 主；稿件可选加入某一合集，或保持未分类（collection_id IS NULL）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 投稿合集表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `video_collections` (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'UP主投稿合集' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 2. videos：归属合集（可空）
-- ----------------------------
ALTER TABLE `videos`
  ADD COLUMN `collection_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '投稿合集ID，NULL 表示未加入合集' AFTER `user_id`;

ALTER TABLE `videos`
  ADD INDEX `idx_videos_collection_id`(`collection_id` ASC) USING BTREE;

ALTER TABLE `videos`
  ADD CONSTRAINT `fk_videos_video_collection`
    FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`)
    ON DELETE SET NULL ON UPDATE RESTRICT;

-- ----------------------------
-- 3. video_submissions：投稿时选择的合集
-- ----------------------------
ALTER TABLE `video_submissions`
  ADD COLUMN `collection_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '投稿选择的合集ID' AFTER `collection_name`;

ALTER TABLE `video_submissions`
  ADD INDEX `idx_video_submissions_collection_id`(`collection_id` ASC) USING BTREE;

ALTER TABLE `video_submissions`
  ADD CONSTRAINT `fk_video_submissions_video_collection`
    FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`)
    ON DELETE SET NULL ON UPDATE RESTRICT;

-- ----------------------------
-- 4. video_drafts：与投稿表同步
-- ----------------------------
ALTER TABLE `video_drafts`
  ADD COLUMN `collection_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '投稿选择的合集ID' AFTER `collection_name`;

ALTER TABLE `video_drafts`
  ADD INDEX `idx_video_drafts_collection_id`(`collection_id` ASC) USING BTREE;

ALTER TABLE `video_drafts`
  ADD CONSTRAINT `fk_video_drafts_video_collection`
    FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`)
    ON DELETE SET NULL ON UPDATE RESTRICT;

SET FOREIGN_KEY_CHECKS = 1;
