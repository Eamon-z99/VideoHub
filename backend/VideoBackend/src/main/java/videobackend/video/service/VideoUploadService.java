package videobackend.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import videobackend.video.model.VideoItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 处理用户上传的视频：保存到本地磁盘 + 写入 videos 表。
 */
@Service
public class VideoUploadService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${media.storage.root}")
    private String mediaStorageRoot;

    public VideoUploadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存视频文件并创建一条视频记录，返回生成的视频 ID。
     * 可选上传封面图片 coverFile，并写入前端传来的时长（秒）。
     */
    public String uploadVideo(Long userId,
                              String title,
                              String description,
                              MultipartFile file,
                              MultipartFile coverFile,
                              Integer durationSeconds,
                              String copyright,
                              String partition,
                              String tagsJson,
                              String scheduleEnabled,
                              String schedulePublishAt,
                              String collectionEnabled,
                              String collectionName,
                              String allowSecondCreation,
                              String commercialPromotion) throws IOException {
        if (userId == null) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传视频文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("只能上传视频文件");
        }

        // 只允许常见的视频扩展名，避免无法播放
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!StringUtils.hasText(ext)) {
            ext = "mp4";
        }
        String lowerExt = ext.toLowerCase();
        if (!lowerExt.equals("mp4") && !lowerExt.equals("mkv") && !lowerExt.equals("mov")) {
            throw new IllegalArgumentException("暂不支持该视频格式，请上传 mp4/mkv/mov");
        }

        // 生成相对路径：uploads/videos/YYYY/MM/userId-uuid.ext
        LocalDateTime now = LocalDateTime.now();
        String relativePath = String.format("uploads/videos/%d/%02d/%d-%s.%s",
                now.getYear(),
                now.getMonthValue(),
                userId,
                UUID.randomUUID().toString().replaceAll("-", ""),
                lowerExt);

        Path root = Paths.get(mediaStorageRoot);
        Path target = root.resolve(relativePath).normalize();
        Files.createDirectories(target.getParent());
        file.transferTo(target.toFile());

        long fileSize = Files.size(target);

        // 处理封面：可选
        String coverRelativePath = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverContentType = coverFile.getContentType();
            if (coverContentType == null || !coverContentType.startsWith("image/")) {
                throw new IllegalArgumentException("封面必须是图片文件");
            }
            String coverExt = getFileExtension(coverFile.getOriginalFilename());
            if (!StringUtils.hasText(coverExt)) {
                coverExt = "jpg";
            }
            coverExt = coverExt.toLowerCase();

            String coverPath = String.format("uploads/covers/%d/%02d/%d-%s.%s",
                    now.getYear(),
                    now.getMonthValue(),
                    userId,
                    UUID.randomUUID().toString().replaceAll("-", ""),
                    coverExt);

            Path coverTarget = root.resolve(coverPath).normalize();
            Files.createDirectories(coverTarget.getParent());
            coverFile.transferTo(coverTarget.toFile());
            coverRelativePath = coverPath;
        }

        // 生成视频 ID（不与现有规则冲突即可）
        String videoId = "uv_" + userId + "_" + System.currentTimeMillis();

        // 写入 videos 表，部分字段使用默认值
        String sql = """
                INSERT INTO videos
                (video_id, title, description, duration, cover_url, storage_path, source_file,
                 view_count, like_count, favorite_count, file_size, user_id, create_time,
                 copyright, `partition`, tags,
                 schedule_enabled, schedule_publish_at,
                 collection_enabled, collection_name,
                 allow_second_creation, commercial_promotion)
                VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?, NOW(),
                        ?, ?, ?,
                        ?, ?,
                        ?, ?,
                        ?, ?)
                """;

        int safeDuration = (durationSeconds != null && durationSeconds > 0) ? durationSeconds : 0;

        // 目前没有转码，如果前端没有提供时长，则先写 0，后续可在转码完成后回填真实时长
        jdbcTemplate.update(sql,
                videoId,
                StringUtils.hasText(title) ? title : defaultTitleFromFilename(originalFilename),
                description,
                safeDuration,           // duration：前端提供的秒数，或 0
                coverRelativePath,      // cover_url：如果有上传封面，则为相对路径，否则为 NULL
                relativePath,           // storage_path：包含相对子目录
                relativePath,           // source_file：保持一致，便于 LocalVideoService 解析
                fileSize,
                userId,
                normalizeCopyright(copyright),
                normalizePartition(partition),
                normalizeTags(tagsJson),
                parseBool01(scheduleEnabled),
                normalizeScheduleTime(schedulePublishAt, scheduleEnabled),
                parseBool01(collectionEnabled),
                normalizeCollectionName(collectionName, collectionEnabled),
                parseBool01(allowSecondCreation),
                parseBool01(commercialPromotion));

        return videoId;
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            return filename.substring(dot + 1);
        }
        return "";
    }

    private String defaultTitleFromFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "用户投稿视频";
        }
        int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        String name = slash >= 0 ? filename.substring(slash + 1) : filename;
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name;
    }

    private Integer parseBool01(String v) {
        if (!StringUtils.hasText(v)) {
            return 0;
        }
        String s = v.trim().toLowerCase();
        if (s.equals("1") || s.equals("true") || s.equals("yes") || s.equals("on")) {
            return 1;
        }
        return 0;
    }

    private String normalizeCopyright(String v) {
        if (!StringUtils.hasText(v)) {
            return "original";
        }
        String s = v.trim().toLowerCase();
        return (s.equals("repost") || s.equals("original")) ? s : "original";
    }

    private String normalizePartition(String v) {
        if (!StringUtils.hasText(v)) {
            return "game";
        }
        return v.trim();
    }

    private String normalizeTags(String tagsJson) {
        if (!StringUtils.hasText(tagsJson)) {
            return "[]";
        }
        String s = tagsJson.trim();
        // 不做 JSON 解析，直接存原串；前端保证是 JSON 数组字符串
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }

    private String normalizeScheduleTime(String schedulePublishAt, String scheduleEnabled) {
        if (parseBool01(scheduleEnabled) == 0) {
            return null;
        }
        if (!StringUtils.hasText(schedulePublishAt)) {
            return null;
        }
        // 前端 value-format: YYYY-MM-DD HH:mm:ss
        return schedulePublishAt.trim();
    }

    private String normalizeCollectionName(String name, String enabled) {
        if (parseBool01(enabled) == 0) {
            return null;
        }
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String s = name.trim();
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
}


