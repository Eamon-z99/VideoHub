-- 用户订阅投稿合集（「订阅合集」/「我追的合集」）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `video_collection_subscriptions` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '订阅用户',
  `collection_id` bigint UNSIGNED NOT NULL COMMENT '投稿合集ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_collection`(`user_id` ASC, `collection_id` ASC) USING BTREE,
  INDEX `idx_sub_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_sub_collection`(`collection_id` ASC) USING BTREE,
  CONSTRAINT `fk_vcs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_vcs_collection` FOREIGN KEY (`collection_id`) REFERENCES `video_collections` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户订阅投稿合集' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
