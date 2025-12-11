package videobackend.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import videobackend.video.model.VideoItem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocalVideoService {

    private final JdbcTemplate jdbcTemplate;
    private final Path mediaRoot;

    public LocalVideoService(JdbcTemplate jdbcTemplate,
                             @Value("${media.storage.root}") String mediaStorageRoot) {
        this.jdbcTemplate = jdbcTemplate;
        this.mediaRoot = Paths.get(mediaStorageRoot);
    }

    public List<VideoItem> listAll() {
        String sql = """
                SELECT video_id, title, description, duration,
                       cover_url, storage_path, source_file,
                       view_count, file_size
                FROM videos
                ORDER BY create_time DESC
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs));
    }

    public Optional<VideoItem> findByVideoId(String videoId) {
        String sql = """
                SELECT video_id, title, description, duration,
                       cover_url, storage_path, source_file,
                       view_count, file_size
                FROM videos
                WHERE video_id = ?
                """;
        List<VideoItem> list = jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), videoId);
        return list.stream().findFirst();
    }

    private VideoItem mapToVideo(ResultSet rs) throws SQLException {
        String sourceFile = rs.getString("source_file");
        String storagePath = rs.getString("storage_path");
        String coverFile = rs.getString("cover_url");
        return new VideoItem(
                rs.getString("video_id"),
                firstNonBlank(rs.getString("title"), "本地视频"),
                rs.getString("description"),
                rs.getObject("duration") == null ? null : rs.getInt("duration"),
                buildLocalUrl(sourceFile, coverFile),
                buildLocalUrl(sourceFile, storagePath),
                storagePath,
                sourceFile,
                rs.getObject("view_count") == null ? null : rs.getLong("view_count"),
                rs.getObject("file_size") == null ? null : rs.getLong("file_size")
        );
    }

    private String buildLocalUrl(String sourceFile, String fileName) {
        if (!StringUtils.hasText(sourceFile) || !StringUtils.hasText(fileName)) {
            return "";
        }
        String normalizedSource = normalizeSource(sourceFile);
        String normalizedFile = fileName.replace("\\", "/");
        String encoded = Arrays.stream((normalizedSource + "/" + normalizedFile).split("/"))
                .filter(StringUtils::hasText)
                .map(seg -> UriUtils.encodePathSegment(seg, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
        return "/local-videos/" + encoded;
    }

    private String normalizeSource(String sourceFile) {
        String normalized = sourceFile.replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!StringUtils.hasText(normalized)) {
            return normalized;
        }
        Path rootFileName = mediaRoot.getFileName();
        if (rootFileName != null) {
            String rootName = rootFileName.toString().toLowerCase(Locale.ROOT);
            if (normalized.toLowerCase(Locale.ROOT).startsWith(rootName + "/")) {
                return normalized.substring(rootName.length() + 1);
            }
        }
        return normalized;
    }

    private String firstNonBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
