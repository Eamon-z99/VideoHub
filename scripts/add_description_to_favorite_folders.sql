-- 为收藏夹表添加描述字段
ALTER TABLE `favorite_folders` 
  ADD COLUMN `description` VARCHAR(200) NULL DEFAULT NULL COMMENT '收藏夹描述' AFTER `is_public`;


