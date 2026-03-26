package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.VideoNoteItem;

import java.sql.Timestamp;
import java.util.List;

@Service
public class VideoNoteService {

    private final JdbcTemplate jdbcTemplate;
    private final LocalVideoService localVideoService;

    public VideoNoteService(JdbcTemplate jdbcTemplate, LocalVideoService localVideoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.localVideoService = localVideoService;
    }

    @Transactional
    public long createNote(Long authorUserId, String videoId, String title, String content, Integer visibility) {
        int vis = visibility == null ? 1 : visibility;
        String normalizedTitle = title == null ? "" : title.trim();
        if (normalizedTitle.isBlank()) {
            normalizedTitle = "公开笔记";
        }
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isBlank()) {
            throw new IllegalArgumentException("笔记内容不能为空");
        }

        String sql = """
                INSERT INTO video_notes
                (video_id, author_user_id, title, content, visibility, status, created_time, update_time)
                VALUES (?, ?, ?, ?, ?, 1, NOW(), NOW())
                """;
        jdbcTemplate.update(sql, videoId, authorUserId, normalizedTitle, normalizedContent, vis);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id == null ? 0L : id;
    }

    public VideoNoteItem getNoteById(Long id) {
        String sql = """
                SELECT vn.id,
                       vn.video_id,
                       v.title AS video_title,
                       v.source_file AS video_source_file,
                       v.cover_url AS video_cover_url,
                       vn.author_user_id,
                       u.username AS author_username,
                       vn.title,
                       vn.content,
                       vn.visibility,
                       vn.status,
                       vn.created_time
                FROM video_notes vn
                JOIN videos v ON v.video_id = vn.video_id
                JOIN users u ON u.id = vn.author_user_id
                WHERE vn.id = ?
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return null;
            Timestamp ts = rs.getTimestamp("created_time");
            String sourceFile = rs.getString("video_source_file");
            String coverFile = rs.getString("video_cover_url");
            String coverUrl = localVideoService.buildCoverPublicUrl(sourceFile, coverFile);
            return new VideoNoteItem(
                    rs.getLong("id"),
                    rs.getString("video_id"),
                    rs.getString("video_title"),
                    coverUrl,
                    rs.getLong("author_user_id"),
                    rs.getString("author_username"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getInt("visibility"),
                    rs.getInt("status"),
                    ts != null ? ts.toString() : null
            );
        }, id);
    }

    public List<VideoNoteItem> listNotesByVideoAndAuthor(Long authorUserId, String videoId) {
        String sql = """
                SELECT vn.id,
                       vn.video_id,
                       v.title AS video_title,
                       v.source_file AS video_source_file,
                       v.cover_url AS video_cover_url,
                       vn.author_user_id,
                       u.username AS author_username,
                       vn.title,
                       vn.content,
                       vn.visibility,
                       vn.status,
                       vn.created_time
                FROM video_notes vn
                         JOIN videos v ON v.video_id = vn.video_id
                         JOIN users u ON u.id = vn.author_user_id
                WHERE vn.author_user_id = ?
                  AND vn.status = 1
                  AND vn.video_id = ?
                ORDER BY vn.created_time DESC
                """;

        return jdbcTemplate.query(sql, (rs, i) -> {
            Timestamp ts = rs.getTimestamp("created_time");
            String sourceFile = rs.getString("video_source_file");
            String coverFile = rs.getString("video_cover_url");
            String coverUrl = localVideoService.buildCoverPublicUrl(sourceFile, coverFile);
            return new VideoNoteItem(
                    rs.getLong("id"),
                    rs.getString("video_id"),
                    rs.getString("video_title"),
                    coverUrl,
                    rs.getLong("author_user_id"),
                    rs.getString("author_username"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getInt("visibility"),
                    rs.getInt("status"),
                    ts != null ? ts.toString() : null
            );
        }, authorUserId, videoId);
    }

    /**
     * 获取当前用户下的全部笔记（跨视频）
     */
    public List<VideoNoteItem> listAllNotesByAuthor(Long authorUserId) {
        String sql = """
                SELECT vn.id,
                       vn.video_id,
                       v.title AS video_title,
                       v.source_file AS video_source_file,
                       v.cover_url AS video_cover_url,
                       vn.author_user_id,
                       u.username AS author_username,
                       vn.title,
                       vn.content,
                       vn.visibility,
                       vn.status,
                       vn.created_time
                FROM video_notes vn
                         JOIN videos v ON v.video_id = vn.video_id
                         JOIN users u ON u.id = vn.author_user_id
                WHERE vn.author_user_id = ?
                  AND vn.status = 1
                ORDER BY vn.created_time DESC
                """;

        return jdbcTemplate.query(sql, (rs, i) -> {
            Timestamp ts = rs.getTimestamp("created_time");
            String sourceFile = rs.getString("video_source_file");
            String coverFile = rs.getString("video_cover_url");
            String coverUrl = localVideoService.buildCoverPublicUrl(sourceFile, coverFile);
            return new VideoNoteItem(
                    rs.getLong("id"),
                    rs.getString("video_id"),
                    rs.getString("video_title"),
                    coverUrl,
                    rs.getLong("author_user_id"),
                    rs.getString("author_username"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getInt("visibility"),
                    rs.getInt("status"),
                    ts != null ? ts.toString() : null
            );
        }, authorUserId);
    }

    /**
     * 更新笔记内容（仅允许笔记作者，且仅更新 content 字段）
     */
    @Transactional
    public int updateNoteContent(Long authorUserId, Long noteId, String content) {
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isBlank()) {
            throw new IllegalArgumentException("笔记内容不能为空");
        }

        String sql = """
                UPDATE video_notes
                SET content = ?,
                    update_time = NOW()
                WHERE id = ?
                  AND author_user_id = ?
                  AND status = 1
                """;

        return jdbcTemplate.update(sql, normalizedContent, noteId, authorUserId);
    }

    /**
     * 删除笔记（软删除：status=0）
     */
    @Transactional
    public int deleteNote(Long authorUserId, Long noteId) {
        String sql = """
                UPDATE video_notes
                SET status = 0,
                    update_time = NOW()
                WHERE id = ?
                  AND author_user_id = ?
                  AND status = 1
                """;
        return jdbcTemplate.update(sql, noteId, authorUserId);
    }
}

