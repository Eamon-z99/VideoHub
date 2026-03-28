-- 稍后再看：用户与视频多对多，同一用户对同一视频仅一条记录
-- MySQL 8+ / MariaDB 10.3+

CREATE TABLE IF NOT EXISTS watch_later (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  video_id VARCHAR(64) NOT NULL COMMENT '视频ID，与 videos.video_id 一致',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_watch_later_user_video (user_id, video_id),
  KEY idx_watch_later_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='稍后再看';
