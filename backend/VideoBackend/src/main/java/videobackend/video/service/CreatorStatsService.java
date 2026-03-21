package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreatorStatsService {

    private final JdbcTemplate jdbcTemplate;

    public CreatorStatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取创作者核心指标概览 + 近 N 天趋势 + 稿件表现
     *
     * 所有统计都是按「UP 主」维度（videos.user_id = creatorId）聚合。
     * 时间范围使用 play_history / fans / likes 等表的 create_time 过滤。
     */
    public Map<String, Object> getOverview(Long creatorId, String rangeKey) {
        int days = switch (rangeKey) {
            case "90d" -> 90;
            case "30d" -> 30;
            default -> 7;
        };

        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime start = end.minusDays(days - 1).withHour(0).withMinute(0).withSecond(0);

        Map<String, Object> result = new HashMap<>();
        result.put("coreMetrics", queryCoreMetrics(creatorId, start, end));
        result.put("trend", queryTrend(creatorId, start, end));
        result.put("works", queryWorkStats(creatorId, start, end));
        return result;
    }

    private String normalizeDayKey(Object dateObj) {
        if (dateObj == null) return null;
        String raw = dateObj.toString();
        // 兼容 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / yyyy-MM-ddTHH:mm:ss 等格式
        return raw.length() >= 10 ? raw.substring(0, 10) : raw;
    }

    private Map<String, Object> queryCoreMetrics(Long creatorId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> data = new HashMap<>();

        // 总播放数：使用 videos.view_count 汇总
        Long totalPlays = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(view_count),0) FROM videos WHERE user_id = ?",
                Long.class,
                creatorId
        );
        data.put("plays", totalPlays != null ? totalPlays : 0L);

        // 空间访客：按播放历史中访问该 UP 视频的去重用户
        Long visitors = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT ph.user_id)
                FROM play_history ph
                JOIN videos v ON ph.video_id = v.video_id
                WHERE v.user_id = ?
                  AND COALESCE(ph.last_watch_time, ph.create_time) BETWEEN ? AND ?
                """,
                Long.class,
                creatorId, start, end
        );
        data.put("visitors", visitors != null ? visitors : 0L);

        // 新增粉丝：fans.following_id = creatorId
        Long newFans = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM fans
                WHERE following_id = ?
                  AND create_time BETWEEN ? AND ?
                """,
                Long.class,
                creatorId, start, end
        );
        data.put("newFans", newFans != null ? newFans : 0L);

        // 点赞
        Long likes = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM video_likes vl
                JOIN videos v ON vl.video_id = v.video_id
                WHERE v.user_id = ?
                  AND vl.create_time BETWEEN ? AND ?
                """,
                Long.class,
                creatorId, start, end
        );
        data.put("likes", likes != null ? likes : 0L);

        // 收藏
        Long favorites = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM favorites f
                JOIN videos v ON f.video_id = v.video_id
                WHERE v.user_id = ?
                  AND f.create_time BETWEEN ? AND ?
                """,
                Long.class,
                creatorId, start, end
        );
        data.put("favorites", favorites != null ? favorites : 0L);

        // 投币：当前没有专门投币表，先返回 0，后续如有表结构可替换
        data.put("coins", 0L);

        // 评论
        Long comments = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM comments c
                JOIN videos v ON c.video_id = v.video_id
                WHERE v.user_id = ?
                  AND c.create_time BETWEEN ? AND ?
                """,
                Long.class,
                creatorId, start, end
        );
        data.put("comments", comments != null ? comments : 0L);

        // 弹幕 / 分享：当前没有统一结构，这里先返回 0
        data.put("danmaku", 0L);
        data.put("shares", 0L);

        return data;
    }

    private Map<String, Object> queryTrend(Long creatorId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> data = new HashMap<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MM-dd");

        // 构建日期数组
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;

        String[] labels = new String[days];
        for (int i = 0; i < days; i++) {
            labels[i] = startDate.plusDays(i).format(dayFmt);
        }
        data.put("labels", labels);

        // 播放趋势（总播放量，按天）
        List<Map<String, Object>> playRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(COALESCE(ph.last_watch_time, ph.create_time)) AS d, COUNT(*) AS cnt
                FROM play_history ph
                JOIN videos v ON ph.video_id = v.video_id
                WHERE v.user_id = ?
                  AND COALESCE(ph.last_watch_time, ph.create_time) BETWEEN ? AND ?
                GROUP BY DATE(COALESCE(ph.last_watch_time, ph.create_time))
                """,
                creatorId, start, end
        );

        Map<String, Long> playMap = new HashMap<>();
        for (Map<String, Object> row : playRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                playMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] totalPlays = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            totalPlays[i] = playMap.getOrDefault(d.toString(), 0L);
        }
        data.put("totalPlays", totalPlays);

        // 粉丝播放：粉丝看视频的次数
        List<Map<String, Object>> fanPlayRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(COALESCE(ph.last_watch_time, ph.create_time)) AS d, COUNT(*) AS cnt
                FROM play_history ph
                JOIN videos v ON ph.video_id = v.video_id
                JOIN fans f ON ph.user_id = f.follower_id AND f.following_id = v.user_id
                WHERE v.user_id = ?
                  AND COALESCE(ph.last_watch_time, ph.create_time) BETWEEN ? AND ?
                GROUP BY DATE(COALESCE(ph.last_watch_time, ph.create_time))
                """,
                creatorId, start, end
        );

        Map<String, Long> fanPlayMap = new HashMap<>();
        for (Map<String, Object> row : fanPlayRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                fanPlayMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] fanPlays = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            fanPlays[i] = fanPlayMap.getOrDefault(d.toString(), 0L);
        }
        data.put("fanPlays", fanPlays);

        // 空间访客：每天去重播放用户数
        List<Map<String, Object>> visitorRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(COALESCE(ph.last_watch_time, ph.create_time)) AS d, COUNT(DISTINCT ph.user_id) AS cnt
                FROM play_history ph
                JOIN videos v ON ph.video_id = v.video_id
                WHERE v.user_id = ?
                  AND COALESCE(ph.last_watch_time, ph.create_time) BETWEEN ? AND ?
                GROUP BY DATE(COALESCE(ph.last_watch_time, ph.create_time))
                """,
                creatorId, start, end
        );

        Map<String, Long> visitorMap = new HashMap<>();
        for (Map<String, Object> row : visitorRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                visitorMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] visitors = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            visitors[i] = visitorMap.getOrDefault(d.toString(), 0L);
        }
        data.put("visitors", visitors);

        // 新增粉丝：按天新增关注人数
        List<Map<String, Object>> newFanRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(f.create_time) AS d, COUNT(*) AS cnt
                FROM fans f
                WHERE f.following_id = ?
                  AND f.create_time BETWEEN ? AND ?
                GROUP BY DATE(f.create_time)
                """,
                creatorId, start, end
        );

        Map<String, Long> newFanMap = new HashMap<>();
        for (Map<String, Object> row : newFanRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                newFanMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] newFans = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            newFans[i] = newFanMap.getOrDefault(d.toString(), 0L);
        }
        data.put("newFans", newFans);

        // 点赞趋势（按天）
        List<Map<String, Object>> likeRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(vl.create_time) AS d, COUNT(*) AS cnt
                FROM video_likes vl
                JOIN videos v ON vl.video_id = v.video_id
                WHERE v.user_id = ?
                  AND vl.create_time BETWEEN ? AND ?
                GROUP BY DATE(vl.create_time)
                """,
                creatorId, start, end
        );

        Map<String, Long> likeMap = new HashMap<>();
        for (Map<String, Object> row : likeRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                likeMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] likes = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            likes[i] = likeMap.getOrDefault(d.toString(), 0L);
        }
        data.put("likes", likes);

        // 收藏趋势（按天）
        List<Map<String, Object>> favoriteRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(f.create_time) AS d, COUNT(*) AS cnt
                FROM favorites f
                JOIN videos v ON f.video_id = v.video_id
                WHERE v.user_id = ?
                  AND f.create_time BETWEEN ? AND ?
                GROUP BY DATE(f.create_time)
                """,
                creatorId, start, end
        );

        Map<String, Long> favoriteMap = new HashMap<>();
        for (Map<String, Object> row : favoriteRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                favoriteMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] favorites = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            favorites[i] = favoriteMap.getOrDefault(d.toString(), 0L);
        }
        data.put("favorites", favorites);

        // 评论趋势（按天）
        List<Map<String, Object>> commentRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(c.create_time) AS d, COUNT(*) AS cnt
                FROM comments c
                JOIN videos v ON c.video_id = v.video_id
                WHERE v.user_id = ?
                  AND c.create_time BETWEEN ? AND ?
                GROUP BY DATE(c.create_time)
                """,
                creatorId, start, end
        );

        Map<String, Long> commentMap = new HashMap<>();
        for (Map<String, Object> row : commentRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                commentMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] comments = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            comments[i] = commentMap.getOrDefault(d.toString(), 0L);
        }
        data.put("comments", comments);

        // 互动量（点赞 + 收藏）
        List<Map<String, Object>> interactionRows = jdbcTemplate.queryForList(
                """
                SELECT d, SUM(cnt) AS total_cnt FROM (
                    SELECT DATE(vl.create_time) AS d, COUNT(*) AS cnt
                    FROM video_likes vl
                    JOIN videos v ON vl.video_id = v.video_id
                    WHERE v.user_id = ?
                      AND vl.create_time BETWEEN ? AND ?
                    GROUP BY DATE(vl.create_time)
                    UNION ALL
                    SELECT DATE(f.create_time) AS d, COUNT(*) AS cnt
                    FROM favorites f
                    JOIN videos v ON f.video_id = v.video_id
                    WHERE v.user_id = ?
                      AND f.create_time BETWEEN ? AND ?
                    GROUP BY DATE(f.create_time)
                ) t
                GROUP BY d
                """,
                creatorId, start, end, creatorId, start, end
        );

        Map<String, Long> interactionMap = new HashMap<>();
        for (Map<String, Object> row : interactionRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("total_cnt");
            if (d != null && cnt != null) {
                interactionMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] interactions = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            interactions[i] = interactionMap.getOrDefault(d.toString(), 0L);
        }
        data.put("interactions", interactions);

        // 弹幕/投币/分享：当前数据库没有对应表结构，这里按 0 返回（保证前端可切换）
        data.put("danmaku", new long[days]);
        data.put("coins", new long[days]);
        data.put("shares", new long[days]);

        return data;
    }

    private List<Map<String, Object>> queryWorkStats(Long creatorId, LocalDateTime start, LocalDateTime end) {
        // 简单给出最近一段时间发布的稿件的聚合表现，方便前端直接渲染表格
        return jdbcTemplate.queryForList(
                """
                SELECT 
                    v.video_id       AS id,
                    v.title          AS title,
                    DATE_FORMAT(v.create_time, '%Y-%m-%d') AS publishTime,
                    COALESCE(v.view_count, 0)             AS plays,
                    (
                        SELECT COUNT(*) FROM video_likes vl WHERE vl.video_id = v.video_id
                    )                                     AS likes,
                    0                                     AS coins,
                    (
                        SELECT COUNT(*) FROM favorites f WHERE f.video_id = v.video_id
                    )                                     AS favorites,
                    NULL                                  AS finishRate
                FROM videos v
                WHERE v.user_id = ?
                  AND v.create_time BETWEEN ? AND ?
                ORDER BY v.create_time DESC
                LIMIT 50
                """,
                creatorId, start, end
        );
    }
}

