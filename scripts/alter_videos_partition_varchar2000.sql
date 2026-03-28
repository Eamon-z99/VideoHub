-- 将 videos.partition 加宽以存放 JSON 数组（assign_video_partitions.py 依赖）
-- 在对应库中执行一次即可。

ALTER TABLE `videos`
MODIFY COLUMN `partition` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
NULL DEFAULT NULL COMMENT '分区导航（JSON 数组，对应首页六个快捷入口）';
