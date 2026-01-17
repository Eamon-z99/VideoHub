package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.PlayHistoryItem;
import videobackend.video.model.VideoItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlayHistoryService {

    private final JdbcTemplate jdbcTemplate;
    private final LocalVideoService localVideoService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PlayHistoryService(JdbcTemplate jdbcTemplate, LocalVideoService localVideoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.localVideoService = localVideoService;
    }

    /**
     * 记录或更新播放历史
     */
    @Transactional
    public void recordPlayHistory(Long userId, String videoId, Integer playTime, Integer duration) {
        // 检查是否已存在记录，同时获取上次的播放时间
        String checkSql = "SELECT id, watch_count, play_time FROM play_history WHERE user_id = ? AND video_id = ?";
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, userId, videoId);

        if (!existing.isEmpty()) {
            // 更新现有记录
            Object idObj = existing.get(0).get("id");
            Long id = null;
            if (idObj != null) {
                if (idObj instanceof java.math.BigInteger) {
                    id = ((java.math.BigInteger) idObj).longValue();
                } else if (idObj instanceof Number) {
                    id = ((Number) idObj).longValue();
                } else {
                    id = Long.valueOf(idObj.toString());
                }
            }
            
            Object watchCountObj = existing.get(0).get("watch_count");
            Integer watchCount = null;
            if (watchCountObj != null) {
                if (watchCountObj instanceof Number) {
                    watchCount = ((Number) watchCountObj).intValue();
                } else {
                    watchCount = Integer.valueOf(watchCountObj.toString());
                }
            }
            if (watchCount == null) watchCount = 1;

            // 获取上次的播放时间
            Object lastPlayTimeObj = existing.get(0).get("play_time");
            Integer lastPlayTime = null;
            if (lastPlayTimeObj != null) {
                if (lastPlayTimeObj instanceof Number) {
                    lastPlayTime = ((Number) lastPlayTimeObj).intValue();
                } else {
                    lastPlayTime = Integer.valueOf(lastPlayTimeObj.toString());
                }
            }
            if (lastPlayTime == null) lastPlayTime = 0;

            // 判断是否是一次新的观看：
            // 1. 如果当前播放时间明显小于上次（说明重新开始播放，回退超过30秒）
            // 2. 或者当前播放时间从0或很小的值开始（小于10秒），且上次播放时间较大（说明重新开始播放）
            boolean isNewWatch = false;
            if (lastPlayTime > 0) {
                // 如果回退超过30秒，认为是重新开始观看
                if (playTime < lastPlayTime - 30) {
                    isNewWatch = true;
                }
                // 如果上次播放时间较大（>60秒），但当前播放时间很小（<10秒），认为是重新开始观看
                else if (lastPlayTime > 60 && playTime < 10) {
                    isNewWatch = true;
                }
            }

            // 计算进度百分比
            Integer progressPercent = 0;
            Boolean isWatched = false;
            if (duration != null && duration > 0) {
                progressPercent = Math.min(100, (int) (playTime * 100.0 / duration));
                isWatched = progressPercent >= 90;
            }

            // 只有在判断为新观看时才增加 watch_count
            String updateSql;
            if (isNewWatch) {
                updateSql = """
                    UPDATE play_history 
                    SET play_time = ?, duration = ?, progress_percent = ?, is_watched = ?,
                        watch_count = watch_count + 1, last_watch_time = NOW(), update_time = NOW()
                    WHERE id = ?
                    """;
            } else {
                updateSql = """
                    UPDATE play_history 
                    SET play_time = ?, duration = ?, progress_percent = ?, is_watched = ?,
                        last_watch_time = NOW(), update_time = NOW()
                    WHERE id = ?
                    """;
            }
            jdbcTemplate.update(updateSql, playTime, duration, progressPercent, isWatched ? 1 : 0, id);
        } else {
            // 插入新记录（首次观看，watch_count = 1）
            Integer progressPercent = 0;
            Boolean isWatched = false;
            if (duration != null && duration > 0) {
                progressPercent = Math.min(100, (int) (playTime * 100.0 / duration));
                isWatched = progressPercent >= 90;
            }

            String insertSql = """
                INSERT INTO play_history (user_id, video_id, play_time, duration, progress_percent, 
                                         is_watched, watch_count, last_watch_time, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, 1, NOW(), NOW(), NOW())
                """;
            jdbcTemplate.update(insertSql, userId, videoId, playTime, duration, 
                              progressPercent, isWatched ? 1 : 0);
        }
    }

    /**
     * 获取用户的播放历史列表（按最后观看时间倒序）
     */
    public List<PlayHistoryItem> getUserHistory(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        String sql = """
            SELECT ph.id, ph.video_id, ph.play_time, ph.duration, ph.progress_percent,
                   ph.is_watched, ph.watch_count, ph.last_watch_time, ph.create_time,
                   v.title, v.cover_url, v.storage_path, v.source_file
            FROM play_history ph
            LEFT JOIN videos v ON ph.video_id = v.video_id
            WHERE ph.user_id = ?
            ORDER BY ph.last_watch_time DESC
            LIMIT ? OFFSET ?
            """;
        return jdbcTemplate.query(sql, this::mapToPlayHistoryItem, userId, pageSize, offset);
    }

    /**
     * 获取用户历史记录总数
     */
    public Long getUserHistoryCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM play_history WHERE user_id = ?";
        Object countObj = jdbcTemplate.queryForObject(sql, Object.class, userId);
        if (countObj == null) {
            return 0L;
        }
        // 处理BigInteger到Long的转换
        if (countObj instanceof java.math.BigInteger) {
            return ((java.math.BigInteger) countObj).longValue();
        } else if (countObj instanceof Number) {
            return ((Number) countObj).longValue();
        }
        return Long.valueOf(countObj.toString());
    }

    /**
     * 删除单条历史记录
     */
    @Transactional
    public boolean deleteHistory(Long userId, Long historyId) {
        String sql = "DELETE FROM play_history WHERE id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, historyId, userId);
        return deleted > 0;
    }

    /**
     * 清空用户所有历史记录
     */
    @Transactional
    public boolean clearAllHistory(Long userId) {
        String sql = "DELETE FROM play_history WHERE user_id = ?";
        int deleted = jdbcTemplate.update(sql, userId);
        return deleted >= 0;
    }

    /**
     * 批量删除历史记录
     */
    @Transactional
    public int batchDeleteHistory(Long userId, List<Long> historyIds) {
        if (historyIds == null || historyIds.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", historyIds.stream().map(id -> "?").toList());
        String sql = "DELETE FROM play_history WHERE id IN (" + placeholders + ") AND user_id = ?";
        List<Object> params = new java.util.ArrayList<>(historyIds);
        params.add(userId);
        return jdbcTemplate.update(sql, params.toArray());
    }

    private PlayHistoryItem mapToPlayHistoryItem(ResultSet rs, int rowNum) throws SQLException {
        String videoId = rs.getString("video_id");
        String storagePath = rs.getString("storage_path");
        String sourceFile = rs.getString("source_file");
        String coverUrl = rs.getString("cover_url");

        // 通过LocalVideoService获取视频信息以构建正确的URL
        String videoUrl = "";
        String coverUrlFinal = "";
        try {
            Optional<VideoItem> videoOpt = localVideoService.findByVideoId(videoId);
            if (videoOpt.isPresent()) {
                VideoItem video = videoOpt.get();
                videoUrl = video.playUrl();
                coverUrlFinal = video.coverUrl();
            } else {
                // 如果找不到视频，使用简化方式构建URL
                videoUrl = buildSimpleUrl(sourceFile, storagePath);
                coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
            }
        } catch (Exception e) {
            // 如果出错，使用简化方式构建URL
            videoUrl = buildSimpleUrl(sourceFile, storagePath);
            coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
        }

        Timestamp lastWatchTime = rs.getTimestamp("last_watch_time");
        Timestamp createTime = rs.getTimestamp("create_time");

        // 安全地读取Long类型的id，处理BigInteger转换
        Long id = null;
        Object idObj = rs.getObject("id");
        if (idObj != null) {
            if (idObj instanceof java.math.BigInteger) {
                id = ((java.math.BigInteger) idObj).longValue();
            } else if (idObj instanceof Number) {
                id = ((Number) idObj).longValue();
            } else {
                id = Long.valueOf(idObj.toString());
            }
        }

        return new PlayHistoryItem(
            id,
            videoId,
            rs.getString("title"),
            coverUrlFinal,
            videoUrl,
            rs.getObject("play_time") != null ? rs.getInt("play_time") : 0,
            rs.getObject("duration") != null ? rs.getInt("duration") : null,
            rs.getObject("progress_percent") != null ? rs.getInt("progress_percent") : 0,
            rs.getObject("is_watched") != null && rs.getInt("is_watched") == 1,
            rs.getObject("watch_count") != null ? rs.getInt("watch_count") : 1,
            lastWatchTime != null ? lastWatchTime.toLocalDateTime().format(DATE_FORMATTER) : null,
            createTime != null ? createTime.toLocalDateTime().format(DATE_FORMATTER) : null
        );
    }

    private String buildSimpleUrl(String sourceFile, String fileName) {
        if (fileName != null) {
            return "/local-videos/" + fileName.replace("\\", "/");
        }
        return "";
    }
}

