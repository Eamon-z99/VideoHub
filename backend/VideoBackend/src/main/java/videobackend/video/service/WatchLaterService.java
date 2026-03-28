package videobackend.video.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.VideoItem;
import videobackend.video.model.WatchLaterItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class WatchLaterService {

    private final JdbcTemplate jdbcTemplate;
    private final LocalVideoService localVideoService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WatchLaterService(JdbcTemplate jdbcTemplate, LocalVideoService localVideoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.localVideoService = localVideoService;
    }

    @Transactional
    public boolean add(Long userId, String videoId) {
        if (userId == null || videoId == null || videoId.isBlank()) {
            return false;
        }
        try {
            String sql = "INSERT INTO watch_later (user_id, video_id, create_time) VALUES (?, ?, NOW())";
            jdbcTemplate.update(sql, userId, videoId.trim());
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Transactional
    public boolean remove(Long userId, String videoId) {
        if (userId == null || videoId == null || videoId.isBlank()) {
            return false;
        }
        String sql = "DELETE FROM watch_later WHERE user_id = ? AND video_id = ?";
        int n = jdbcTemplate.update(sql, userId, videoId.trim());
        return n > 0;
    }

    @Transactional
    public boolean removeById(Long userId, Long id) {
        if (userId == null || id == null) {
            return false;
        }
        String sql = "DELETE FROM watch_later WHERE user_id = ? AND id = ?";
        int n = jdbcTemplate.update(sql, userId, id);
        return n > 0;
    }

    public boolean contains(Long userId, String videoId) {
        if (userId == null || videoId == null || videoId.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM watch_later WHERE user_id = ? AND video_id = ?";
        Object c = jdbcTemplate.queryForObject(sql, Object.class, userId, videoId.trim());
        if (c == null) return false;
        long n = (c instanceof Number) ? ((Number) c).longValue() : Long.parseLong(c.toString());
        return n > 0;
    }

    public long countByUser(Long userId) {
        if (userId == null) return 0;
        String sql = "SELECT COUNT(*) FROM watch_later WHERE user_id = ?";
        Object c = jdbcTemplate.queryForObject(sql, Object.class, userId);
        if (c == null) return 0;
        if (c instanceof Number) return ((Number) c).longValue();
        return Long.parseLong(c.toString());
    }

    public List<WatchLaterItem> list(Long userId, int page, int pageSize) {
        if (userId == null) {
            return Collections.emptyList();
        }
        int p = Math.max(1, page);
        int ps = Math.min(Math.max(1, pageSize), 100);
        int offset = (p - 1) * ps;

        String sql = """
                SELECT w.id, w.video_id, w.create_time,
                       v.title, v.cover_url, v.duration, v.storage_path, v.source_file,
                       u.username AS uploader_name
                FROM watch_later w
                LEFT JOIN videos v ON w.video_id = v.video_id
                LEFT JOIN users u ON v.user_id = u.id
                WHERE w.user_id = ?
                ORDER BY w.create_time DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapRow(rs), userId, ps, offset);
    }

    private WatchLaterItem mapRow(ResultSet rs) throws SQLException {
        WatchLaterItem item = new WatchLaterItem();
        String videoId = rs.getString("video_id");
        String storagePath = rs.getString("storage_path");
        String sourceFile = rs.getString("source_file");
        String coverUrl = rs.getString("cover_url");
        Integer durationSeconds = rs.getObject("duration") != null ? rs.getInt("duration") : null;

        String videoUrl = "";
        String coverUrlFinal = "";
        if (videoId != null) {
            try {
                Optional<VideoItem> videoOpt = localVideoService.findByVideoId(videoId);
                if (videoOpt.isPresent()) {
                    VideoItem video = videoOpt.get();
                    videoUrl = video.playUrl();
                    coverUrlFinal = video.coverUrl();
                } else {
                    videoUrl = buildSimpleUrl(sourceFile, storagePath);
                    coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
                }
            } catch (Exception e) {
                videoUrl = buildSimpleUrl(sourceFile, storagePath);
                coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
            }
        } else {
            videoUrl = buildSimpleUrl(sourceFile, storagePath);
            coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
        }

        Object idObj = rs.getObject("id");
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
        item.setId(id);
        item.setVideoId(videoId != null ? videoId : "");
        String title = rs.getString("title");
        item.setTitle(title != null ? title : "未知标题");
        item.setCoverUrl(coverUrlFinal != null ? coverUrlFinal : "");
        item.setVideoUrl(videoUrl != null ? videoUrl : "");
        item.setDuration(formatDuration(durationSeconds));
        Timestamp createTime = rs.getTimestamp("create_time");
        item.setCreateTime(createTime != null ? createTime.toLocalDateTime().format(DATE_FORMATTER) : null);
        item.setUploaderName(rs.getString("uploader_name"));
        return item;
    }

    private static String formatDuration(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "--:--";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }

    private String buildSimpleUrl(String sourceFile, String fileName) {
        if (fileName != null) {
            return "/local-videos/" + fileName.replace("\\", "/");
        }
        return "";
    }
}
