-- 推荐系统性能优化：为行为表添加常用索引
-- 适用：MySQL 8.x（无 IF NOT EXISTS 时请确保索引名不冲突/未重复创建）
--
-- 说明：
-- - User-CF/行为矩阵：常按 user_id 聚合、按 video_id 取用户集合
-- - Item-CF：会按 video_id 找 user_id，再 join 到用户的其它 video_id
--
-- 执行前建议：
-- - 先在测试库运行
-- - 观察执行时间（大表加索引可能较久）

-- video_likes
CREATE INDEX idx_video_likes_user_video ON video_likes(user_id, video_id);
CREATE INDEX idx_video_likes_video_user ON video_likes(video_id, user_id);

-- favorites
CREATE INDEX idx_favorites_user_video ON favorites(user_id, video_id);
CREATE INDEX idx_favorites_video_user ON favorites(video_id, user_id);

-- play_history
CREATE INDEX idx_play_history_user_video ON play_history(user_id, video_id);
CREATE INDEX idx_play_history_video_user ON play_history(video_id, user_id);
CREATE INDEX idx_play_history_video_play_time ON play_history(video_id, play_time);


