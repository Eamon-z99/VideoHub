-- 二次回复：记录被 @ 的用户（对「回复」再回复时使用）
ALTER TABLE `comments`
  ADD COLUMN `reply_to_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '被回复用户ID（仅二次回复）' AFTER `parent_id`;

ALTER TABLE `comments`
  ADD INDEX `idx_reply_to_user_id` (`reply_to_user_id` ASC);

ALTER TABLE `comments`
  ADD CONSTRAINT `comments_ibfk_reply_to_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT;
