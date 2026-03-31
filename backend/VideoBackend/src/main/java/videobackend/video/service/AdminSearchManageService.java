package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class AdminSearchManageService {

    private final JdbcTemplate jdbcTemplate;

    public AdminSearchManageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> listKeywordStats(int page, int pageSize, String keywordLike) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String like = StringUtils.hasText(keywordLike) ? keywordLike.trim() : null;
        String likeParam = like == null ? null : ("%" + like + "%");

        String where = likeParam == null ? "" : "WHERE e.keyword LIKE ?";

        List<Map<String, Object>> list = jdbcTemplate.queryForList("""
                SELECT e.keyword,
                       SUM(e.weight) AS total_count,
                       COUNT(*) AS event_count,
                       COUNT(DISTINCT e.user_id) AS user_count,
                       MIN(e.create_time) AS first_seen_time,
                       MAX(e.create_time) AS last_seen_time,
                       SUM(CASE WHEN e.create_time >= NOW() - INTERVAL 24 HOUR THEN e.weight ELSE 0 END) AS count_24h,
                       hk.score AS score_60m,
                       hk.first_seen_time AS hk_first_seen_time,
                       hk.last_seen_time AS hk_last_seen_time
                FROM search_keyword_events e
                LEFT JOIN search_hot_keywords hk ON hk.keyword = e.keyword
                """ + where + """
                GROUP BY e.keyword
                ORDER BY last_seen_time DESC
                LIMIT ? OFFSET ?
                """, likeParam == null ? new Object[]{safeSize, offset} : new Object[]{likeParam, safeSize, offset});

        Long total = jdbcTemplate.queryForObject(
                (likeParam == null
                        ? "SELECT COUNT(DISTINCT keyword) FROM search_keyword_events"
                        : "SELECT COUNT(DISTINCT keyword) FROM search_keyword_events WHERE keyword LIKE ?"),
                Long.class,
                likeParam == null ? new Object[]{} : new Object[]{likeParam}
        );

        return Map.of(
                "list", list,
                "page", safePage,
                "pageSize", safeSize,
                "total", total == null ? 0 : total
        );
    }

    public Map<String, Object> listKeywordEvents(String keyword, int page, int pageSize) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("keyword 不能为空");
        }
        int safeSize = Math.max(1, Math.min(pageSize, 200));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        String kw = keyword.trim();
        List<Map<String, Object>> list = jdbcTemplate.queryForList("""
                SELECT e.id,
                       e.keyword,
                       e.weight,
                       e.create_time,
                       e.user_id,
                       u.username,
                       u.avatar
                FROM search_keyword_events e
                LEFT JOIN users u ON e.user_id = u.id
                WHERE e.keyword = ?
                ORDER BY e.create_time DESC, e.id DESC
                LIMIT ? OFFSET ?
                """, kw, safeSize, offset);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM search_keyword_events WHERE keyword = ?",
                Long.class,
                kw
        );

        return Map.of(
                "list", list,
                "page", safePage,
                "pageSize", safeSize,
                "total", total == null ? 0 : total
        );
    }

    public Map<String, Object> listUserEvents(Long userId, int page, int pageSize) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId 无效");
        }
        int safeSize = Math.max(1, Math.min(pageSize, 200));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;

        List<Map<String, Object>> list = jdbcTemplate.queryForList("""
                SELECT e.id,
                       e.keyword,
                       e.weight,
                       e.create_time,
                       e.user_id,
                       u.username,
                       u.avatar
                FROM search_keyword_events e
                LEFT JOIN users u ON e.user_id = u.id
                WHERE e.user_id = ?
                ORDER BY e.create_time DESC, e.id DESC
                LIMIT ? OFFSET ?
                """, userId, safeSize, offset);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM search_keyword_events WHERE user_id = ?",
                Long.class,
                userId
        );

        return Map.of(
                "list", list,
                "page", safePage,
                "pageSize", safeSize,
                "total", total == null ? 0 : total
        );
    }

    public List<Map<String, Object>> getHotSlots() {
        return jdbcTemplate.queryForList("""
                SELECT slot, keyword, badge, update_time
                FROM admin_hot_search_slots
                ORDER BY slot ASC
                """);
    }

    @Transactional
    public void saveHotSlots(List<Map<String, Object>> slots) {
        if (slots == null) {
            throw new IllegalArgumentException("slots 不能为空");
        }
        // 允许传入 0-10 条；slot 重复以最后一次为准
        Map<Integer, Map<String, Object>> bySlot = new HashMap<>();
        for (Map<String, Object> s : slots) {
            Object slotObj = s.get("slot");
            if (slotObj == null) continue;
            int slot = Integer.parseInt(String.valueOf(slotObj));
            if (slot < 1 || slot > 10) {
                throw new IllegalArgumentException("slot 必须在 1-10");
            }
            bySlot.put(slot, s);
        }

        // 清空全部槽位（避免唯一索引 keyword 造成更新顺序冲突）
        jdbcTemplate.update("UPDATE admin_hot_search_slots SET keyword=NULL, badge='' WHERE slot BETWEEN 1 AND 10");

        // 写入
        Set<String> usedKeywords = new HashSet<>();
        for (int slot = 1; slot <= 10; slot++) {
            Map<String, Object> s = bySlot.get(slot);
            if (s == null) continue;
            String kw = s.get("keyword") == null ? null : String.valueOf(s.get("keyword")).trim();
            String badge = s.get("badge") == null ? "" : String.valueOf(s.get("badge")).trim().toUpperCase();
            if (!StringUtils.hasText(kw)) {
                kw = null;
            }
            if (!badge.equals("") && !badge.equals("NEW") && !badge.equals("HOT")) {
                throw new IllegalArgumentException("badge 只允许 NEW/HOT/空");
            }
            if (kw != null) {
                if (!usedKeywords.add(kw)) {
                    throw new IllegalArgumentException("keyword 不能重复：" + kw);
                }
            }
            jdbcTemplate.update("""
                    INSERT INTO admin_hot_search_slots (slot, keyword, badge)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE keyword=VALUES(keyword), badge=VALUES(badge)
                    """, slot, kw, badge);
        }
    }
}

