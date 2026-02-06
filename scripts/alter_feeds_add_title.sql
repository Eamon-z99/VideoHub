USE videohub;

ALTER TABLE `feeds`
  ADD COLUMN `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '动态标题'
  AFTER `user_id`;


