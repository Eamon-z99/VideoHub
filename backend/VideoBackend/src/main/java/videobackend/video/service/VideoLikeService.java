package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.VideoItem;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VideoLikeService {

    private final JdbcTemplate jdbcTemplate;
    private final LocalVideoService localVideoService;

    public VideoLikeService(JdbcTemplate jdbcTemplate, LocalVideoService localVideoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.localVideoService = localVideoService;
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

    /**
     * 获取用户点赞的视频列表（按点赞时间倒序）
     */
    public List<Map<String, Object>> getUserLikedVideos(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        String sql = """
                SELECT vl.id, vl.video_id, vl.create_time,
                       v.title, v.cover_url, v.duration, v.storage_path, v.source_file,
                       v.view_count, v.like_count,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar,
                       u.id AS uploader_id
                FROM video_likes vl
                LEFT JOIN videos v ON vl.video_id = v.video_id
                LEFT JOIN users u ON v.user_id = u.id
                WHERE vl.user_id = ?
                ORDER BY vl.create_time DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> {
            String videoId = rs.getString("video_id");
            String storagePath = rs.getString("storage_path");
            String sourceFile = rs.getString("source_file");
            String coverUrl = rs.getString("cover_url");
            
            // 通过LocalVideoService获取视频信息以构建正确的URL
            String coverUrlFinal = "";
            try {
                Optional<VideoItem> videoOpt = localVideoService.findByVideoId(videoId);
                if (videoOpt.isPresent()) {
                    VideoItem video = videoOpt.get();
                    coverUrlFinal = video.coverUrl();
                } else {
                    // 如果找不到视频，使用简化方式构建URL
                    coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
                }
            } catch (Exception e) {
                // 如果出错，使用简化方式构建URL
                coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
            }
            
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", rs.getLong("id"));
            item.put("videoId", videoId);
            item.put("title", rs.getString("title"));
            item.put("coverUrl", coverUrlFinal);
            item.put("duration", formatDuration(rs.getInt("duration")));
            item.put("storagePath", storagePath);
            item.put("sourceFile", sourceFile);
            item.put("viewCount", rs.getLong("view_count"));
            item.put("likeCount", rs.getLong("like_count"));
            item.put("uploaderName", rs.getString("uploader_name"));
            item.put("uploaderAvatar", rs.getString("uploader_avatar"));
            item.put("uploaderId", rs.getLong("uploader_id"));
            Timestamp createTime = rs.getTimestamp("create_time");
            item.put("createTime", createTime != null ? createTime.toString() : null);
            return item;
        }, userId, pageSize, offset);
    }

    /**
     * 获取用户点赞总数
     */
    public Long getUserLikedCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM video_likes WHERE user_id = ?";
        Object countObj = jdbcTemplate.queryForObject(sql, Object.class, userId);
        if (countObj == null) {
            return 0L;
        }
        if (countObj instanceof java.math.BigInteger) {
            return ((java.math.BigInteger) countObj).longValue();
        } else if (countObj instanceof Number) {
            return ((Number) countObj).longValue();
        }
        return Long.valueOf(countObj.toString());
    }

    /**
     * 格式化视频时长（秒 -> MM:SS 或 HH:MM:SS）
     */
    private String formatDuration(int seconds) {
        if (seconds < 0) return "00:00";
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%d:%02d", minutes, secs);
    }

    private String buildSimpleUrl(String sourceFile, String fileName) {
        if (fileName != null) {
            return "/local-videos/" + fileName.replace("\\", "/");
        }
        return "";
    }
}


