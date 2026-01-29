package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videobackend.video.model.FavoriteFolderItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FavoriteFolderService {

    public static final String DEFAULT_FOLDER_NAME = "默认收藏夹";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public FavoriteFolderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 确保用户存在一个“默认收藏夹”，并返回其 id
     */
    @Transactional
    public Long ensureDefaultFolder(Long userId) {
        Optional<Long> existing = findFolderIdByName(userId, DEFAULT_FOLDER_NAME);
        if (existing.isPresent()) {
            return existing.get();
        }

        String insertSql = "INSERT INTO favorite_folders (user_id, name, is_public, create_time, update_time) VALUES (?, ?, 1, NOW(), NOW())";
        jdbcTemplate.update(insertSql, userId, DEFAULT_FOLDER_NAME);

        return findFolderIdByName(userId, DEFAULT_FOLDER_NAME)
                .orElseThrow(() -> new RuntimeException("创建默认收藏夹失败"));
    }

    public Optional<Long> findFolderIdByName(Long userId, String name) {
        String sql = "SELECT id FROM favorite_folders WHERE user_id = ? AND name = ? LIMIT 1";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId, name);
        if (rows.isEmpty()) return Optional.empty();
        Object idObj = rows.get(0).get("id");
        if (idObj == null) return Optional.empty();
        if (idObj instanceof java.math.BigInteger) return Optional.of(((java.math.BigInteger) idObj).longValue());
        if (idObj instanceof Number) return Optional.of(((Number) idObj).longValue());
        return Optional.of(Long.valueOf(idObj.toString()));
    }

    /**
     * 获取用户收藏夹列表（带数量）
     */
    public List<FavoriteFolderItem> listFolders(Long userId) {
        // 将 favorites.folder_id 为空的记录归到默认收藏夹（通过 defaultFolderId）
        Long defaultFolderId = ensureDefaultFolder(userId);
        String sql = """
                SELECT ff.id, ff.user_id, ff.name, ff.is_public, ff.create_time,
                       (
                         SELECT COUNT(*) FROM favorites f
                         WHERE f.user_id = ff.user_id
                           AND (
                             (ff.id = ? AND (f.folder_id IS NULL OR f.folder_id = ff.id))
                             OR (ff.id <> ? AND f.folder_id = ff.id)
                           )
                       ) AS cnt
                FROM favorite_folders ff
                WHERE ff.user_id = ?
                ORDER BY (ff.id = ?) DESC, ff.create_time DESC
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapFolder(rs), defaultFolderId, defaultFolderId, userId, defaultFolderId);
    }

    /**
     * 创建收藏夹
     */
    @Transactional
    public Long createFolder(Long userId, String name, Boolean isPublic) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("收藏夹名称不能为空");
        }
        String safeName = name.trim();

        // 保证默认收藏夹存在
        ensureDefaultFolder(userId);

        String insertSql = "INSERT INTO favorite_folders (user_id, name, is_public, create_time, update_time) VALUES (?, ?, ?, NOW(), NOW())";
        jdbcTemplate.update(insertSql, userId, safeName, (isPublic != null && !isPublic) ? 0 : 1);

        return findFolderIdByName(userId, safeName)
                .orElseThrow(() -> new RuntimeException("创建收藏夹失败"));
    }

    /**
     * 重命名收藏夹
     */
    @Transactional
    public boolean renameFolder(Long userId, Long folderId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("新名称不能为空");
        }
        String safeName = newName.trim();

        // 不允许重命名默认收藏夹（按名称判断，简单可靠）
        String checkSql = "SELECT name FROM favorite_folders WHERE id = ? AND user_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(checkSql, folderId, userId);
        if (rows.isEmpty()) return false;
        String oldName = String.valueOf(rows.get(0).get("name"));
        if (DEFAULT_FOLDER_NAME.equals(oldName)) {
            throw new IllegalArgumentException("默认收藏夹不允许重命名");
        }

        String updateSql = "UPDATE favorite_folders SET name = ?, update_time = NOW() WHERE id = ? AND user_id = ?";
        int updated = jdbcTemplate.update(updateSql, safeName, folderId, userId);
        return updated > 0;
    }

    /**
     * 删除收藏夹：将其下的 favorites.folder_id 置空（回到默认收藏夹逻辑），再删除收藏夹
     */
    @Transactional
    public boolean deleteFolder(Long userId, Long folderId) {
        // 不允许删除默认收藏夹
        String checkSql = "SELECT name FROM favorite_folders WHERE id = ? AND user_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(checkSql, folderId, userId);
        if (rows.isEmpty()) return false;
        String name = String.valueOf(rows.get(0).get("name"));
        if (DEFAULT_FOLDER_NAME.equals(name)) {
            throw new IllegalArgumentException("默认收藏夹不允许删除");
        }

        // 迁移收藏记录回默认（置空）
        String moveSql = "UPDATE favorites SET folder_id = NULL WHERE user_id = ? AND folder_id = ?";
        jdbcTemplate.update(moveSql, userId, folderId);

        String deleteSql = "DELETE FROM favorite_folders WHERE id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(deleteSql, folderId, userId);
        return deleted > 0;
    }

    private FavoriteFolderItem mapFolder(ResultSet rs) throws SQLException {
        Object idObj = rs.getObject("id");
        Long id;
        if (idObj instanceof java.math.BigInteger) id = ((java.math.BigInteger) idObj).longValue();
        else if (idObj instanceof Number) id = ((Number) idObj).longValue();
        else id = Long.valueOf(String.valueOf(idObj));

        Object uidObj = rs.getObject("user_id");
        Long userId;
        if (uidObj instanceof java.math.BigInteger) userId = ((java.math.BigInteger) uidObj).longValue();
        else if (uidObj instanceof Number) userId = ((Number) uidObj).longValue();
        else userId = Long.valueOf(String.valueOf(uidObj));

        Object cntObj = rs.getObject("cnt");
        Long cnt = 0L;
        if (cntObj instanceof java.math.BigInteger) cnt = ((java.math.BigInteger) cntObj).longValue();
        else if (cntObj instanceof Number) cnt = ((Number) cntObj).longValue();
        else if (cntObj != null) cnt = Long.valueOf(cntObj.toString());

        Integer isPublicInt = rs.getObject("is_public") != null ? rs.getInt("is_public") : 1;
        Timestamp createTime = rs.getTimestamp("create_time");
        return new FavoriteFolderItem(
                id,
                userId,
                rs.getString("name"),
                isPublicInt == 1,
                cnt,
                createTime != null ? createTime.toLocalDateTime().format(DATE_FORMATTER) : null
        );
    }
}


