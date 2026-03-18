package videobackend.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 投稿/审核流：用户上传写入 video_submissions；管理端审核通过后再发布到 videos。
 */
@Service
public class VideoSubmissionService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${media.storage.root}")
    private String mediaStorageRoot;

    public VideoSubmissionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String createSubmission(Long userId,
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
                                   String commercialPromotion,
                                   boolean draftOnly) throws IOException {
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

        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!StringUtils.hasText(ext)) {
            ext = "mp4";
        }
        String lowerExt = ext.toLowerCase();
        if (!lowerExt.equals("mp4") && !lowerExt.equals("mkv") && !lowerExt.equals("mov")) {
            throw new IllegalArgumentException("暂不支持该视频格式，请上传 mp4/mkv/mov");
        }

        LocalDateTime now = LocalDateTime.now();
        Path root = Paths.get(mediaStorageRoot);

        String baseDir = draftOnly ? "uploads/videoDrafts" : "uploads/submissions";
        String relativePath = String.format(baseDir + "/videos/%d/%02d/%d-%s.%s",
                now.getYear(),
                now.getMonthValue(),
                userId,
                UUID.randomUUID().toString().replaceAll("-", ""),
                lowerExt);
        Path target = root.resolve(relativePath).normalize();
        Files.createDirectories(target.getParent());
        file.transferTo(target.toFile());
        long fileSize = Files.size(target);

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
            String coverPath = String.format(baseDir + "/covers/%d/%02d/%d-%s.%s",
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

        String submissionId = "sub_" + userId + "_" + System.currentTimeMillis();
        int safeDuration = (durationSeconds != null && durationSeconds > 0) ? durationSeconds : 0;

        String sql = """
                INSERT INTO video_submissions
                (submission_id, user_id, title, description,
                 copyright, `partition`, tags,
                 duration, cover_url, storage_path, source_file, file_size,
                 schedule_enabled, schedule_publish_at,
                 collection_enabled, collection_name,
                 allow_second_creation, commercial_promotion,
                 review_status, create_time, update_time)
                VALUES
                (?, ?, ?, ?,
                 ?, ?, ?,
                 ?, ?, ?, ?, ?,
                 ?, ?,
                 ?, ?,
                 ?, ?,
                 'DRAFT', NOW(), NOW())
                """;

        jdbcTemplate.update(sql,
                submissionId,
                userId,
                StringUtils.hasText(title) ? title.trim() : defaultTitleFromFilename(originalFilename),
                StringUtils.hasText(description) ? description : null,
                normalizeCopyright(copyright),
                StringUtils.hasText(partition) ? partition.trim() : null,
                normalizeTags(tagsJson),
                safeDuration,
                coverRelativePath,
                relativePath,
                relativePath,
                fileSize,
                parseBool01(scheduleEnabled),
                normalizeScheduleTime(schedulePublishAt, scheduleEnabled),
                parseBool01(collectionEnabled),
                normalizeCollectionName(collectionName, collectionEnabled),
                parseBool01(allowSecondCreation),
                parseBool01(commercialPromotion)
        );

        return submissionId;
    }

    /**
     * 二步投稿：上传后更新投稿信息（不重新上传视频文件）。
     */
    @Transactional
    public void updateSubmission(String submissionId,
                                 Long userId,
                                 String title,
                                 String description,
                                 MultipartFile coverFile,
                                 String copyright,
                                 String partition,
                                 String tagsJson,
                                 String scheduleEnabled,
                                 String schedulePublishAt,
                                 String collectionEnabled,
                                 String collectionName,
                                 String allowSecondCreation,
                                 String commercialPromotion,
                                 boolean submitNow) throws IOException {
        if (!StringUtils.hasText(submissionId)) {
            throw new IllegalArgumentException("投稿ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("未登录或登录已过期");
        }

        LocalDateTime now = LocalDateTime.now();
        Path root = Paths.get(mediaStorageRoot);

        // 根据当前投稿的视频存储路径，决定封面存储目录（草稿与正式投稿分开）
        String currentStoragePath = null;
        String currentCoverUrl = null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap("""
                    SELECT storage_path, cover_url
                    FROM video_submissions
                    WHERE submission_id=? AND user_id=?
                    """, submissionId.trim(), userId);
            currentStoragePath = row.get("storage_path") == null ? null : String.valueOf(row.get("storage_path"));
            currentCoverUrl = row.get("cover_url") == null ? null : String.valueOf(row.get("cover_url"));
        } catch (Exception ignore) {
        }
        boolean isDraftStorage = currentStoragePath != null && currentStoragePath.replace('\\', '/').startsWith("uploads/videoDrafts/");
        // 提交投稿时：即使当前在草稿目录，也要把新封面写到正式目录
        String coverBaseDir = (submitNow ? "uploads/submissions" : (isDraftStorage ? "uploads/videoDrafts" : "uploads/submissions"));

        // 如果是从草稿提交为投稿：把视频文件（以及已有封面）从 videoDrafts 迁移到 submissions
        String migratedStoragePath = null;
        String migratedSourceFile = null;
        String migratedCoverUrl = null;
        if (submitNow && isDraftStorage && StringUtils.hasText(currentStoragePath)) {
            String srcStorage = currentStoragePath.replace('\\', '/').trim();
            String dstStorage = srcStorage.replaceFirst("^uploads/videoDrafts/", "uploads/submissions/");

            Path srcVideo = root.resolve(srcStorage).normalize();
            Path dstVideo = root.resolve(dstStorage).normalize();
            Files.createDirectories(dstVideo.getParent());
            try {
                Files.move(srcVideo, dstVideo);
            } catch (IOException e) {
                // Windows 上跨盘符 move 可能失败，fallback copy+delete
                Files.copy(srcVideo, dstVideo);
                Files.deleteIfExists(srcVideo);
            }

            migratedStoragePath = dstStorage;
            migratedSourceFile = dstStorage;

            // 只有在“没有上传新封面”的情况下，才迁移旧封面；新封面会在下面直接写入 submissions
            if ((coverFile == null || coverFile.isEmpty()) && StringUtils.hasText(currentCoverUrl)) {
                String srcCover = currentCoverUrl.replace('\\', '/').trim();
                if (srcCover.startsWith("uploads/videoDrafts/")) {
                    String dstCover = srcCover.replaceFirst("^uploads/videoDrafts/", "uploads/submissions/");
                    Path srcCoverPath = root.resolve(srcCover).normalize();
                    Path dstCoverPath = root.resolve(dstCover).normalize();
                    Files.createDirectories(dstCoverPath.getParent());
                    try {
                        Files.move(srcCoverPath, dstCoverPath);
                    } catch (IOException e) {
                        Files.copy(srcCoverPath, dstCoverPath);
                        Files.deleteIfExists(srcCoverPath);
                    }
                    migratedCoverUrl = dstCover;
                }
            }
        }

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
            String coverPath = String.format(coverBaseDir + "/covers/%d/%02d/%d-%s.%s",
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

        int updated = jdbcTemplate.update("""
                        UPDATE video_submissions
                        SET title=?,
                            description=?,
                            copyright=?,
                            `partition`=?,
                            tags=?,
                            schedule_enabled=?,
                            schedule_publish_at=?,
                            collection_enabled=?,
                            collection_name=?,
                            allow_second_creation=?,
                            commercial_promotion=?,
                            storage_path=COALESCE(?, storage_path),
                            source_file=COALESCE(?, source_file),
                            cover_url=COALESCE(?, COALESCE(?, cover_url)),
                            review_status=CASE
                                WHEN ? = 1 AND review_status = 'DRAFT' THEN 'PENDING'
                                ELSE review_status
                            END,
                            update_time=NOW()
                        WHERE submission_id=?
                          AND user_id=?
                          AND review_status IN ('DRAFT','PENDING')
                        """,
                StringUtils.hasText(title) ? title.trim() : "用户投稿视频",
                StringUtils.hasText(description) ? description.trim() : null,
                normalizeCopyright(copyright),
                StringUtils.hasText(partition) ? partition.trim() : null,
                normalizeTags(tagsJson),
                parseBool01(scheduleEnabled),
                normalizeScheduleTime(schedulePublishAt, scheduleEnabled),
                parseBool01(collectionEnabled),
                normalizeCollectionName(collectionName, collectionEnabled),
                parseBool01(allowSecondCreation),
                parseBool01(commercialPromotion),
                migratedStoragePath,
                migratedSourceFile,
                coverRelativePath,
                migratedCoverUrl,
                submitNow ? 1 : 0,
                submissionId.trim(),
                userId
        );
        if (updated <= 0) {
            throw new IllegalArgumentException("投稿不存在、无权限或状态不可编辑（可能已被处理）");
        }

        // 草稿表同步：如果是提交投稿（DRAFT->PENDING），则草稿箱移除；否则更新草稿表
        if (submitNow) {
            jdbcTemplate.update("DELETE FROM video_drafts WHERE submission_id=? AND user_id=?", submissionId.trim(), userId);
        } else {
            jdbcTemplate.update("""
                            INSERT INTO video_drafts
                            (submission_id, user_id, title, description,
                             copyright, `partition`, tags,
                             duration, cover_url, storage_path, source_file, file_size,
                             schedule_enabled, schedule_publish_at,
                             collection_enabled, collection_name,
                             allow_second_creation, commercial_promotion,
                             create_time, update_time)
                            SELECT s.submission_id, s.user_id, s.title, s.description,
                                   s.copyright, s.`partition`, s.tags,
                                   s.duration, s.cover_url, s.storage_path, s.source_file, s.file_size,
                                   s.schedule_enabled, s.schedule_publish_at,
                                   s.collection_enabled, s.collection_name,
                                   s.allow_second_creation, s.commercial_promotion,
                                   NOW(), NOW()
                            FROM video_submissions s
                            WHERE s.submission_id=? AND s.user_id=? AND s.review_status='DRAFT'
                            ON DUPLICATE KEY UPDATE
                                title=VALUES(title),
                                description=VALUES(description),
                                copyright=VALUES(copyright),
                                `partition`=VALUES(`partition`),
                                tags=VALUES(tags),
                                duration=VALUES(duration),
                                cover_url=VALUES(cover_url),
                                storage_path=VALUES(storage_path),
                                source_file=VALUES(source_file),
                                file_size=VALUES(file_size),
                                schedule_enabled=VALUES(schedule_enabled),
                                schedule_publish_at=VALUES(schedule_publish_at),
                                collection_enabled=VALUES(collection_enabled),
                                collection_name=VALUES(collection_name),
                                allow_second_creation=VALUES(allow_second_creation),
                                commercial_promotion=VALUES(commercial_promotion),
                                update_time=NOW()
                            """, submissionId.trim(), userId);
        }
    }

    public Map<String, Object> listPending(int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String sql = """
                SELECT s.submission_id,
                       s.title,
                       s.`partition`,
                       s.tags,
                       s.duration,
                       s.cover_url,
                       s.create_time,
                       u.id AS uploader_id,
                       u.username AS uploader_name,
                       u.avatar AS uploader_avatar
                FROM video_submissions s
                LEFT JOIN users u ON s.user_id = u.id
                WHERE s.review_status = 'PENDING'
                ORDER BY s.create_time DESC
                LIMIT ? OFFSET ?
                """;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, safeSize, offset);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM video_submissions WHERE review_status='PENDING'",
                Long.class
        );
        return Map.of(
                "list", list,
                "page", safePage,
                "pageSize", safeSize,
                "total", total == null ? 0 : total
        );
    }

    @Transactional
    public void approve(String submissionId, Long reviewerId, String comment) {
        int updated = jdbcTemplate.update("""
                        UPDATE video_submissions
                        SET review_status='APPROVED',
                            reviewer_id=?,
                            review_comment=?,
                            review_time=NOW(),
                            update_time=NOW()
                        WHERE submission_id=?
                          AND review_status='PENDING'
                        """,
                reviewerId,
                StringUtils.hasText(comment) ? comment.trim() : null,
                submissionId
        );
        if (updated <= 0) {
            throw new IllegalArgumentException("投稿不存在或状态不可审核（可能已被处理）");
        }
    }

    @Transactional
    public void reject(String submissionId, Long reviewerId, String comment) {
        if (!StringUtils.hasText(comment)) {
            throw new IllegalArgumentException("驳回原因不能为空");
        }
        int updated = jdbcTemplate.update("""
                        UPDATE video_submissions
                        SET review_status='REJECTED',
                            reviewer_id=?,
                            review_comment=?,
                            review_time=NOW(),
                            update_time=NOW()
                        WHERE submission_id=?
                          AND review_status='PENDING'
                        """,
                reviewerId,
                comment.trim(),
                submissionId
        );
        if (updated <= 0) {
            throw new IllegalArgumentException("投稿不存在或状态不可审核（可能已被处理）");
        }
    }

    /**
     * 发布单条：要求已审核通过且未发布；若为定时发布且未到时间，force=false 会拒绝。
     */
    @Transactional
    public String publishOne(String submissionId, Long operatorId, boolean force) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT submission_id, user_id, title, description, duration,
                       cover_url, storage_path, source_file, file_size,
                       schedule_enabled, schedule_publish_at,
                       review_status, published_video_id
                FROM video_submissions
                WHERE submission_id=?
                """, submissionId);

        String status = String.valueOf(row.get("review_status"));
        Object published = row.get("published_video_id");
        if (!"APPROVED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("当前投稿未审核通过，无法发布");
        }
        if (published != null && StringUtils.hasText(String.valueOf(published))) {
            throw new IllegalArgumentException("该投稿已发布，无需重复发布");
        }

        int scheduleEnabled = ((Number) row.getOrDefault("schedule_enabled", 0)).intValue();
        Timestamp publishAt = (Timestamp) row.get("schedule_publish_at");
        if (!force && scheduleEnabled == 1 && publishAt != null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (publishAt.after(now)) {
                throw new IllegalArgumentException("未到定时发布时间，无法发布");
            }
        }

        String videoId = "uv_" + row.get("user_id") + "_" + System.currentTimeMillis();

        // 为兼容旧 videos 表结构，这里只写入基础字段
        jdbcTemplate.update("""
                        INSERT INTO videos
                        (video_id, title, description, duration, cover_url, storage_path, source_file,
                         view_count, like_count, favorite_count, file_size, user_id, create_time)
                        VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?, NOW())
                        """,
                videoId,
                row.get("title"),
                row.get("description"),
                row.get("duration"),
                row.get("cover_url"),
                row.get("storage_path"),
                row.get("source_file"),
                row.get("file_size"),
                row.get("user_id")
        );

        jdbcTemplate.update("""
                        UPDATE video_submissions
                        SET published_video_id=?,
                            publish_time=NOW(),
                            update_time=NOW()
                        WHERE submission_id=?
                        """,
                videoId,
                submissionId
        );
        return videoId;
    }

    /**
     * 批量发布到点的定时投稿（以及非定时但未发布的已通过投稿）。
     */
    @Transactional
    public Map<String, Object> publishDue(int limit, Long operatorId) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<Map<String, Object>> due = jdbcTemplate.queryForList("""
                SELECT submission_id
                FROM video_submissions
                WHERE review_status='APPROVED'
                  AND (published_video_id IS NULL OR published_video_id = '')
                  AND (
                        schedule_enabled = 0
                        OR schedule_publish_at IS NULL
                        OR schedule_publish_at <= NOW()
                  )
                ORDER BY create_time ASC
                LIMIT ?
                """, safeLimit);

        int success = 0;
        for (Map<String, Object> m : due) {
            String sid = String.valueOf(m.get("submission_id"));
            publishOne(sid, operatorId, true);
            success++;
        }
        return Map.of(
                "processed", due.size(),
                "published", success
        );
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

    private String normalizeTags(String tagsJson) {
        if (!StringUtils.hasText(tagsJson)) {
            return "[]";
        }
        String s = tagsJson.trim();
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }

    private Timestamp normalizeScheduleTime(String schedulePublishAt, String scheduleEnabled) {
        if (parseBool01(scheduleEnabled) == 0) {
            return null;
        }
        if (!StringUtils.hasText(schedulePublishAt)) {
            return null;
        }
        // 期望格式：YYYY-MM-DD HH:mm:ss（由前端传入）
        try {
            String t = schedulePublishAt.trim().replace('T', ' ');
            return Timestamp.valueOf(t);
        } catch (Exception e) {
            return null;
        }
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
}

