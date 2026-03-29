package videobackend.video.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UP 主投稿合集：创建、列表（含已发布稿件数）、校验归属。
 */
@Service
public class VideoCollectionService {

    private final JdbcTemplate jdbcTemplate;

    public VideoCollectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 校验合集属于该用户；不存在或不匹配返回 false。
     */
    public boolean isOwnedByUser(Long collectionId, Long userId) {
        if (collectionId == null || userId == null) {
            return false;
        }
        try {
            Long uid = jdbcTemplate.queryForObject(
                    "SELECT user_id FROM video_collections WHERE id=?",
                    Long.class,
                    collectionId
            );
            return userId.equals(uid);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * 某用户的合集列表 + 每个合集内已发布（可见）视频数；另返回未加入合集的数量。
     */
    public Map<String, Object> listWithCounts(long userId) {
        String sql = """
                SELECT c.id,
                       c.name,
                       c.description,
                       c.sort_order,
                       c.create_time,
                       COUNT(v.video_id) AS video_count
                FROM video_collections c
                LEFT JOIN videos v
                  ON v.collection_id = c.id
                 AND v.user_id = c.user_id
                 AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                WHERE c.user_id = ?
                GROUP BY c.id, c.name, c.description, c.sort_order, c.create_time
                ORDER BY c.sort_order ASC, c.id ASC
                """;
        List<Map<String, Object>> collections = jdbcTemplate.queryForList(sql, userId);

        Long uncategorized = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM videos v
                WHERE v.user_id = ?
                  AND v.collection_id IS NULL
                  AND (v.status IS NULL OR UPPER(TRIM(v.status)) NOT IN ('FAILED','DOWN'))
                """,
                Long.class,
                userId
        );

        Map<String, Object> data = new HashMap<>();
        data.put("collections", collections);
        data.put("uncategorizedCount", uncategorized == null ? 0L : uncategorized);
        return data;
    }

    public long create(Long userId, String name, String description) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("合集名称不能为空");
        }
        String n = name.trim();
        if (n.length() > 100) {
            n = n.substring(0, 100);
        }
        String desc = StringUtils.hasText(description) ? description.trim() : null;
        if (desc != null && desc.length() > 500) {
            desc = desc.substring(0, 500);
        }
        jdbcTemplate.update("""
                        INSERT INTO video_collections (user_id, name, description, sort_order, create_time, update_time)
                        VALUES (?, ?, ?, 0, NOW(), NOW())
                        """,
                userId, n, desc
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id == null ? 0L : id;
    }

    public void update(Long userId, long collectionId, String name, String description) {
        if (!isOwnedByUser(collectionId, userId)) {
            throw new IllegalArgumentException("合集不存在或无权限");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("合集名称不能为空");
        }
        String n = name.trim();
        if (n.length() > 100) {
            n = n.substring(0, 100);
        }
        String desc = StringUtils.hasText(description) ? description.trim() : null;
        if (desc != null && desc.length() > 500) {
            desc = desc.substring(0, 500);
        }
        jdbcTemplate.update("""
                        UPDATE video_collections
                        SET name=?, description=?, update_time=NOW()
                        WHERE id=? AND user_id=?
                        """,
                n, desc, collectionId, userId
        );
    }

    public void delete(Long userId, long collectionId) {
        if (!isOwnedByUser(collectionId, userId)) {
            throw new IllegalArgumentException("合集不存在或无权限");
        }
        jdbcTemplate.update("DELETE FROM video_collections WHERE id=? AND user_id=?", collectionId, userId);
    }

    /**
     * 将已发布视频移入指定投稿合集，或移出合集（collectionId 为 null）。
     */
    public void setVideoCollection(Long userId, String videoId, Long collectionId) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (!StringUtils.hasText(videoId)) {
            throw new IllegalArgumentException("视频ID不能为空");
        }
        String vid = videoId.trim();
        Long cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM videos WHERE video_id=? AND user_id=?",
                Long.class,
                vid,
                userId
        );
        if (cnt == null || cnt == 0) {
            throw new IllegalArgumentException("视频不存在或无权限");
        }
        if (collectionId != null) {
            if (!isOwnedByUser(collectionId, userId)) {
                throw new IllegalArgumentException("合集不存在或无权限");
            }
        }
        jdbcTemplate.update(
                "UPDATE videos SET collection_id=?, update_time=NOW() WHERE video_id=? AND user_id=?",
                collectionId,
                vid,
                userId
        );
    }
}
