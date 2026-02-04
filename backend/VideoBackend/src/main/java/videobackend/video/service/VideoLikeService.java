package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VideoLikeService {

    private final JdbcTemplate jdbcTemplate;

    public VideoLikeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 点赞视频
     *
     * @return true 表示本次操作新增了点赞，false 表示之前已经点过赞
     */
    @Transactional
    public boolean likeVideo(Long userId, String videoId) {
        if (userId == null || videoId == null) {
            throw new IllegalArgumentException("用户ID和视频ID不能为空");
        }

        // 检查是否已点赞
        String checkSql = "SELECT COUNT(*) FROM video_likes WHERE user_id = ? AND video_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, videoId);
        if (count != null && count > 0) {
            return false;
        }

        // 新增点赞记录
        String insertSql = "INSERT INTO video_likes (user_id, video_id, create_time) VALUES (?, ?, NOW())";
        int inserted = jdbcTemplate.update(insertSql, userId, videoId);

        // 更新视频的点赞数
        if (inserted > 0) {
            String updateCountSql = "UPDATE videos SET like_count = like_count + 1 WHERE video_id = ?";
            jdbcTemplate.update(updateCountSql, videoId);
            return true;
        }
        return false;
    }

    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlikeVideo(Long userId, String videoId) {
        if (userId == null || videoId == null) {
            throw new IllegalArgumentException("用户ID和视频ID不能为空");
        }

        String deleteSql = "DELETE FROM video_likes WHERE user_id = ? AND video_id = ?";
        int deleted = jdbcTemplate.update(deleteSql, userId, videoId);

        if (deleted > 0) {
            String updateCountSql = "UPDATE videos SET like_count = GREATEST(like_count - 1, 0) WHERE video_id = ?";
            jdbcTemplate.update(updateCountSql, videoId);
            return true;
        }
        return false;
    }

    /**
     * 是否已点赞
     */
    public boolean isLiked(Long userId, String videoId) {
        if (userId == null || videoId == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM video_likes WHERE user_id = ? AND video_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, videoId);
        return count != null && count > 0;
    }

    /**
     * 获取视频的点赞数
     * 优先使用 videos.like_count 字段
     */
    public long getLikeCount(String videoId) {
        if (videoId == null) return 0;
        String sql = "SELECT like_count FROM videos WHERE video_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, videoId);
        return count != null ? count : 0L;
    }
}


