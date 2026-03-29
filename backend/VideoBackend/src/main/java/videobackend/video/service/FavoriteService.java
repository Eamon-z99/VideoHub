package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.FavoriteItem;
import videobackend.video.model.VideoItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

@Service
public class FavoriteService {

    private final JdbcTemplate jdbcTemplate;
    private final LocalVideoService localVideoService;
    private final FavoriteFolderService favoriteFolderService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FavoriteService(JdbcTemplate jdbcTemplate, LocalVideoService localVideoService, FavoriteFolderService favoriteFolderService) {
        this.jdbcTemplate = jdbcTemplate;
        this.localVideoService = localVideoService;
        this.favoriteFolderService = favoriteFolderService;
    }

    /**
     * 添加收藏
     */
    @Transactional
    public Map<String, Object> addFavorite(Long userId, String videoId, Long folderId) {
        // 确保默认收藏夹存在（并用于兜底）
        Long defaultFolderId = favoriteFolderService.ensureDefaultFolder(userId);
        Long targetFolderId = (folderId != null) ? folderId : defaultFolderId;

        // 检查是否已收藏（唯一键：user_id + video_id）
        String checkSql = "SELECT id, folder_id FROM favorites WHERE user_id = ? AND video_id = ?";
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, userId, videoId);

        if (!existing.isEmpty()) {
            // 已收藏：允许“移动到另一个收藏夹”
            Object idObj = existing.get(0).get("id");
            Long favId = idObj instanceof java.math.BigInteger ? ((java.math.BigInteger) idObj).longValue()
                    : (idObj instanceof Number ? ((Number) idObj).longValue() : Long.valueOf(String.valueOf(idObj)));

            Object oldFolderObj = existing.get(0).get("folder_id");
            Long oldFolderId = null;
            if (oldFolderObj != null) {
                oldFolderId = oldFolderObj instanceof java.math.BigInteger ? ((java.math.BigInteger) oldFolderObj).longValue()
                        : (oldFolderObj instanceof Number ? ((Number) oldFolderObj).longValue() : Long.valueOf(String.valueOf(oldFolderObj)));
            }
            // null 等价于默认收藏夹
            if (oldFolderId == null) oldFolderId = defaultFolderId;

            if (!oldFolderId.equals(targetFolderId)) {
                String updateSql = "UPDATE favorites SET folder_id = ? WHERE id = ? AND user_id = ?";
                jdbcTemplate.update(updateSql, targetFolderId, favId, userId);
                return Map.of("created", false, "moved", true, "favoriteId", favId, "folderId", targetFolderId);
            }
            return Map.of("created", false, "moved", false, "favoriteId", favId, "folderId", targetFolderId);
        }

        // 插入新收藏记录
        String insertSql = "INSERT INTO favorites (user_id, video_id, folder_id, create_time) VALUES (?, ?, ?, NOW())";
        int inserted = jdbcTemplate.update(insertSql, userId, videoId, targetFolderId);

        // 更新视频的收藏数
        if (inserted > 0) {
            String updateCountSql = "UPDATE videos SET favorite_count = favorite_count + 1 WHERE video_id = ?";
            jdbcTemplate.update(updateCountSql, videoId);
        }

        return Map.of("created", inserted > 0, "moved", false, "folderId", targetFolderId);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public boolean removeFavorite(Long userId, String videoId) {
        // 删除收藏记录
        String deleteSql = "DELETE FROM favorites WHERE user_id = ? AND video_id = ?";
        int deleted = jdbcTemplate.update(deleteSql, userId, videoId);
        
        // 更新视频的收藏数
        if (deleted > 0) {
            String updateCountSql = "UPDATE videos SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE video_id = ?";
            jdbcTemplate.update(updateCountSql, videoId);
        }
        
        return deleted > 0;
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long userId, String videoId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND video_id = ?";
        Object countObj = jdbcTemplate.queryForObject(sql, Object.class, userId, videoId);
        
        if (countObj == null) {
            return false;
        }
        
        long count = 0;
        if (countObj instanceof java.math.BigInteger) {
            count = ((java.math.BigInteger) countObj).longValue();
        } else if (countObj instanceof Number) {
            count = ((Number) countObj).longValue();
        } else {
            count = Long.parseLong(countObj.toString());
        }
        
        return count > 0;
    }

    /**
     * 获取用户的收藏列表。
     * sort: favorite_time（最近收藏）| view_count（最多播放）| video_time（最近投稿，按视频发布时间）
     * keyword: 可选，匹配视频标题 / 描述 / UP 昵称
     */
    public List<FavoriteItem> getUserFavorites(Long userId, Long folderId, Integer page, Integer pageSize, Long viewerUserId,
                                                String sort, String keyword) {
        try {
            boolean isOwner = viewerUserId != null && viewerUserId.equals(userId);
            Long defaultFolderIdForSql = isOwner
                    ? favoriteFolderService.ensureDefaultFolder(userId)
                    : favoriteFolderService.findDefaultFolderIdOnly(userId).orElse(0L);

            Long targetFolderId;
            if (folderId != null) {
                targetFolderId = folderId;
            } else {
                targetFolderId = isOwner
                        ? defaultFolderIdForSql
                        : favoriteFolderService.findDefaultFolderIdOnly(userId).orElse(null);
            }

            if (targetFolderId == null) {
                return Collections.emptyList();
            }
            if (!isOwner && !favoriteFolderService.isFolderPublicForViewer(userId, targetFolderId)) {
                return Collections.emptyList();
            }

            int offset = (page - 1) * pageSize;
            String orderBy = resolveFavoriteSortOrder(sort);
            boolean hasKeyword = StringUtils.hasText(keyword);
            String like = hasKeyword ? "%" + keyword.trim() + "%" : "";

            String keywordClause = hasKeyword
                    ? """
                      AND (
                        v.title LIKE ?
                        OR COALESCE(v.description, '') LIKE ?
                        OR COALESCE(u.username, '') LIKE ?
                      )
                      """
                    : "";

            String sql = """
                    SELECT f.id, f.video_id, f.create_time AS favorite_time,
                           v.title, v.cover_url, v.duration, v.storage_path, v.source_file,
                           COALESCE(v.view_count, 0) AS view_count,
                           v.create_time AS video_create_time,
                           u.username AS uploader_name,
                           (SELECT COUNT(*) FROM comments c
                            WHERE c.video_id = v.video_id AND c.status = 1) AS comment_count
                    FROM favorites f
                    LEFT JOIN videos v ON f.video_id = v.video_id
                    LEFT JOIN users u ON v.user_id = u.id
                    WHERE f.user_id = ?
                      AND (
                            (? = ? AND (f.folder_id IS NULL OR f.folder_id = ?))
                            OR (? <> ? AND f.folder_id = ?)
                          )
                    """ + keywordClause + orderBy + """
                    LIMIT ? OFFSET ?
                    """;

            if (hasKeyword) {
                return jdbcTemplate.query(sql, (rs, i) -> mapToFavoriteItem(rs),
                        userId,
                        targetFolderId, defaultFolderIdForSql, defaultFolderIdForSql,
                        targetFolderId, defaultFolderIdForSql, targetFolderId,
                        like, like, like,
                        pageSize, offset);
            }
            return jdbcTemplate.query(sql, (rs, i) -> mapToFavoriteItem(rs),
                    userId,
                    targetFolderId, defaultFolderIdForSql, defaultFolderIdForSql,
                    targetFolderId, defaultFolderIdForSql, targetFolderId,
                    pageSize, offset);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取收藏列表失败: " + e.getMessage(), e);
        }
    }

    private static String resolveFavoriteSortOrder(String sort) {
        if (!StringUtils.hasText(sort)) {
            return " ORDER BY f.create_time DESC ";
        }
        switch (sort.trim().toLowerCase()) {
            case "view_count":
                return " ORDER BY COALESCE(v.view_count, 0) DESC, f.create_time DESC ";
            case "video_time":
                return " ORDER BY (v.create_time IS NULL) ASC, v.create_time DESC, f.create_time DESC ";
            case "favorite_time":
            default:
                return " ORDER BY f.create_time DESC ";
        }
    }

    /**
     * 获取用户收藏总数（keyword 与列表接口一致，用于搜索时分页 total）
     */
    public Long getUserFavoriteCount(Long userId, Long folderId, Long viewerUserId, String keyword) {
        try {
            boolean isOwner = viewerUserId != null && viewerUserId.equals(userId);
            Long defaultFolderIdForSql = isOwner
                    ? favoriteFolderService.ensureDefaultFolder(userId)
                    : favoriteFolderService.findDefaultFolderIdOnly(userId).orElse(0L);

            Long targetFolderId;
            if (folderId != null) {
                targetFolderId = folderId;
            } else {
                targetFolderId = isOwner
                        ? defaultFolderIdForSql
                        : favoriteFolderService.findDefaultFolderIdOnly(userId).orElse(null);
            }

            if (targetFolderId == null) {
                return 0L;
            }
            if (!isOwner && !favoriteFolderService.isFolderPublicForViewer(userId, targetFolderId)) {
                return 0L;
            }

            boolean hasKeyword = StringUtils.hasText(keyword);
            String like = hasKeyword ? "%" + keyword.trim() + "%" : "";

            String keywordClause = hasKeyword
                    ? """
                      AND (
                        v.title LIKE ?
                        OR COALESCE(v.description, '') LIKE ?
                        OR COALESCE(u.username, '') LIKE ?
                      )
                      """
                    : "";

            String sql;
            Object countObj;
            if (hasKeyword) {
                sql = """
                        SELECT COUNT(DISTINCT f.id)
                        FROM favorites f
                        LEFT JOIN videos v ON f.video_id = v.video_id
                        LEFT JOIN users u ON v.user_id = u.id
                        WHERE f.user_id = ?
                          AND (
                                (? = ? AND (f.folder_id IS NULL OR f.folder_id = ?))
                                OR (? <> ? AND f.folder_id = ?)
                              )
                        """ + keywordClause;
                countObj = jdbcTemplate.queryForObject(sql, Object.class,
                        userId,
                        targetFolderId, defaultFolderIdForSql, defaultFolderIdForSql,
                        targetFolderId, defaultFolderIdForSql, targetFolderId,
                        like, like, like);
            } else {
                sql = """
                        SELECT COUNT(*) FROM favorites
                        WHERE user_id = ?
                          AND (
                                (? = ? AND (folder_id IS NULL OR folder_id = ?))
                                OR (? <> ? AND folder_id = ?)
                              )
                        """;
                countObj = jdbcTemplate.queryForObject(sql, Object.class,
                        userId,
                        targetFolderId, defaultFolderIdForSql, defaultFolderIdForSql,
                        targetFolderId, defaultFolderIdForSql, targetFolderId);
            }
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
        } catch (Exception e) {
            e.printStackTrace();
            return 0L; // 出错时返回0，避免影响主流程
        }
    }

    /**
     * 删除单条收藏记录
     */
    @Transactional
    public boolean deleteFavorite(Long userId, Long favoriteId) {
        // 先获取video_id，以便更新收藏数
        String getVideoIdSql = "SELECT video_id FROM favorites WHERE id = ? AND user_id = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(getVideoIdSql, favoriteId, userId);
        
        if (result.isEmpty()) {
            return false;
        }
        
        String videoId = (String) result.get(0).get("video_id");
        
        // 删除收藏记录
        String deleteSql = "DELETE FROM favorites WHERE id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(deleteSql, favoriteId, userId);
        
        // 更新视频的收藏数
        if (deleted > 0 && videoId != null) {
            String updateCountSql = "UPDATE videos SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE video_id = ?";
            jdbcTemplate.update(updateCountSql, videoId);
        }
        
        return deleted > 0;
    }

    /**
     * 批量删除收藏记录
     */
    @Transactional
    public int batchDeleteFavorites(Long userId, List<Long> favoriteIds) {
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return 0;
        }
        
        // 先获取所有video_id，以便更新收藏数
        String placeholders = String.join(",", favoriteIds.stream().map(id -> "?").toList());
        String getVideoIdsSql = "SELECT DISTINCT video_id FROM favorites WHERE id IN (" + placeholders + ") AND user_id = ?";
        List<Object> params = new java.util.ArrayList<>(favoriteIds);
        params.add(userId);
        List<Map<String, Object>> videoIds = jdbcTemplate.queryForList(getVideoIdsSql, params.toArray());
        
        // 删除收藏记录
        String deleteSql = "DELETE FROM favorites WHERE id IN (" + placeholders + ") AND user_id = ?";
        int deleted = jdbcTemplate.update(deleteSql, params.toArray());
        
        // 更新每个视频的收藏数
        if (deleted > 0) {
            for (Map<String, Object> row : videoIds) {
                String videoId = (String) row.get("video_id");
                if (videoId != null) {
                    String updateCountSql = "UPDATE videos SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE video_id = ?";
                    jdbcTemplate.update(updateCountSql, videoId);
                }
            }
        }
        
        return deleted;
    }

    /**
     * 格式化时长（秒转 MM:SS 或 HH:MM:SS）
     */
    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "00:00";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    private FavoriteItem mapToFavoriteItem(ResultSet rs) throws SQLException {
        String videoId = rs.getString("video_id");
        String storagePath = rs.getString("storage_path");
        String sourceFile = rs.getString("source_file");
        String coverUrl = rs.getString("cover_url");
        Integer durationSeconds = rs.getObject("duration") != null ? rs.getInt("duration") : null;

        // 通过LocalVideoService获取视频信息以构建正确的URL
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
                    // 如果找不到视频，使用简化方式构建URL
                    videoUrl = buildSimpleUrl(sourceFile, storagePath);
                    coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
                }
            } catch (Exception e) {
                // 如果出错，使用简化方式构建URL
                videoUrl = buildSimpleUrl(sourceFile, storagePath);
                coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
            }
        } else {
            // videoId 为 null，使用简化方式构建URL
            videoUrl = buildSimpleUrl(sourceFile, storagePath);
            coverUrlFinal = buildSimpleUrl(sourceFile, coverUrl);
        }

        Timestamp favoriteTime = rs.getTimestamp("favorite_time");
        Timestamp videoCreateTs = rs.getTimestamp("video_create_time");

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

        String title = rs.getString("title");
        if (title == null) {
            title = "未知标题";
        }

        Integer viewCount = null;
        Object vcObj = rs.getObject("view_count");
        if (vcObj != null) {
            if (vcObj instanceof Number) {
                viewCount = ((Number) vcObj).intValue();
            } else {
                viewCount = Integer.parseInt(vcObj.toString());
            }
        }

        Integer commentCount = null;
        Object ccObj = rs.getObject("comment_count");
        if (ccObj != null) {
            if (ccObj instanceof Number) {
                commentCount = ((Number) ccObj).intValue();
            } else {
                commentCount = Integer.parseInt(ccObj.toString());
            }
        }

        String uploaderName = rs.getString("uploader_name");
        if (uploaderName != null) {
            uploaderName = uploaderName.trim();
        }

        FavoriteItem item = new FavoriteItem(
            id,
            videoId != null ? videoId : "",
            title,
            coverUrlFinal != null ? coverUrlFinal : "",
            videoUrl != null ? videoUrl : "",
            formatDuration(durationSeconds),
            favoriteTime != null ? favoriteTime.toLocalDateTime().format(DATE_FORMATTER) : null
        );
        item.setUploaderName(uploaderName != null && !uploaderName.isEmpty() ? uploaderName : null);
        item.setViewCount(viewCount);
        item.setCommentCount(commentCount);
        item.setVideoCreateTime(videoCreateTs != null ? videoCreateTs.toLocalDateTime().format(DATE_FORMATTER) : null);
        return item;
    }

    private String buildSimpleUrl(String sourceFile, String fileName) {
        if (fileName != null) {
            return "/local-videos/" + fileName.replace("\\", "/");
        }
        return "";
    }
}

