package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import videobackend.video.dto.DanmakuDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreatorStatsService {

    private final JdbcTemplate jdbcTemplate;
    private final DanmakuService danmakuService;

    public CreatorStatsService(JdbcTemplate jdbcTemplate, DanmakuService danmakuService) {
        this.jdbcTemplate = jdbcTemplate;
        this.danmakuService = danmakuService;
    }

    /**
     * 获取创作者核心指标概览 + 近 N 天趋势 + 稿件表现
     *
     * 所有统计都是按「UP 主」维度（videos.user_id = creatorId）聚合。
     * 时间范围使用 play_history / fans / likes 等表的 create_time 过滤。
     */
    public Map<String, Object> getOverview(Long creatorId, String rangeKey) {
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime start;
        if ("all".equals(rangeKey)) {
            start = queryEarliestDataTime(creatorId);
        } else {
            int days = switch (rangeKey) {
                case "90d" -> 90;
                case "30d" -> 30;
                default -> 7;
            };
            start = end.minusDays(days - 1).withHour(0).withMinute(0).withSecond(0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("coreMetrics", queryCoreMetrics(creatorId, start, end));
        result.put("trend", queryTrend(creatorId, start, end));
        result.put("works", queryWorkStats(creatorId, start, end));
        return result;
    }

    private LocalDateTime queryEarliestDataTime(Long creatorId) {
        LocalDate danmakuEarliestDate = queryEarliestDanmakuDate(creatorId);
        LocalDate earliestDate = jdbcTemplate.queryForObject(
                """
                SELECT MIN(d) FROM (
                    SELECT MIN(pv.visit_date) AS d
                    FROM profile_visits pv
                    WHERE pv.profile_user_id = ?
                    UNION ALL
                    SELECT MIN(DATE(vpe.played_at)) AS d
                    FROM video_play_events vpe
                    WHERE vpe.creator_id = ?
                    UNION ALL
                    SELECT MIN(DATE(ph.create_time)) AS d
                    FROM play_history ph
                    JOIN videos v ON ph.video_id = v.video_id
                    WHERE v.user_id = ?
                    UNION ALL
                    SELECT MIN(DATE(f.create_time)) AS d
                    FROM fans f
                    WHERE f.following_id = ?
                    UNION ALL
                    SELECT MIN(DATE(vl.create_time)) AS d
                    FROM video_likes vl
                    JOIN videos v ON vl.video_id = v.video_id
                    WHERE v.user_id = ?
                    UNION ALL
                    SELECT MIN(DATE(vc.create_time)) AS d
                    FROM video_coins vc
                    JOIN videos v ON vc.video_id = v.video_id
                    WHERE v.user_id = ?
                    UNION ALL
                    SELECT MIN(DATE(fa.create_time)) AS d
                    FROM favorites fa
                    JOIN videos v ON fa.video_id = v.video_id
                    WHERE v.user_id = ?
                    UNION ALL
                    SELECT MIN(DATE(c.create_time)) AS d
                    FROM comments c
                    JOIN videos v ON c.video_id = v.video_id
                    WHERE v.user_id = ?
                ) t
                """,
                LocalDate.class,
                creatorId, creatorId, creatorId, creatorId, creatorId, creatorId, creatorId, creatorId
        );
        if (danmakuEarliestDate != null && (earliestDate == null || danmakuEarliestDate.isBefore(earliestDate))) {
            earliestDate = danmakuEarliestDate;
        }

        if (earliestDate == null) {
            earliestDate = LocalDate.now().minusDays(6);
        }
        return earliestDate.atStartOfDay();
    }

    private List<String> queryCreatorVideoIds(Long creatorId) {
        return jdbcTemplate.query(
                "SELECT video_id FROM videos WHERE user_id = ?",
                (rs, rowNum) -> rs.getString("video_id"),
                creatorId
        );
    }

    private LocalDate queryEarliestDanmakuDate(Long creatorId) {
        List<String> videoIds = queryCreatorVideoIds(creatorId);
        if (videoIds.isEmpty()) return null;

        LocalDate earliest = null;
        ZoneId zone = ZoneId.systemDefault();
        for (String videoId : videoIds) {
            List<DanmakuDTO> list = danmakuService.listAllDanmaku(videoId);
            for (DanmakuDTO dto : list) {
                Long sendTime = dto.getSendTime();
                if (sendTime == null || sendTime <= 0) continue;
                LocalDate d = java.time.Instant.ofEpochMilli(sendTime).atZone(zone).toLocalDate();
                if (earliest == null || d.isBefore(earliest)) {
                    earliest = d;
                }
            }
        }
        return earliest;
    }

    private String normalizeDayKey(Object dateObj) {
        if (dateObj == null) return null;
        String raw = dateObj.toString();
        // 兼容 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / yyyy-MM-ddTHH:mm:ss 等格式
        return raw.length() >= 10 ? raw.substring(0, 10) : raw;
    }

    private Map<String, Object> queryCoreMetrics(Long creatorId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> data = new HashMap<>();

        // 播放量总量：基于播放事件流水，确保与按日趋势口径一致
        Long totalPlays = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM video_play_events WHERE creator_id = ?",
                Long.class,
                creatorId
        );
        data.put("plays", totalPlays != null ? totalPlays : 0L);

        // 空间访客总量：访问过该创作者个人主页的去重用户数
        Long visitors = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT pv.visitor_id)
                FROM profile_visits pv
                WHERE pv.profile_user_id = ?
                """,
                Long.class,
                creatorId
        );
        data.put("visitors", visitors != null ? visitors : 0L);

        // 粉丝总量：历史累计（不受时间范围影响）
        Long newFans = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM fans
                WHERE following_id = ?
                """,
                Long.class,
                creatorId
        );
        data.put("newFans", newFans != null ? newFans : 0L);

        // 点赞总量（不受时间范围影响）
        Long likes = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM video_likes vl
                JOIN videos v ON vl.video_id = v.video_id
                WHERE v.user_id = ?
                """,
                Long.class,
                creatorId
        );
        data.put("likes", likes != null ? likes : 0L);

        // 收藏总量（不受时间范围影响）
        Long favorites = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM favorites f
                JOIN videos v ON f.video_id = v.video_id
                WHERE v.user_id = ?
                """,
                Long.class,
                creatorId
        );
        data.put("favorites", favorites != null ? favorites : 0L);

        // 投币总量
        Long coins = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM video_coins vc
                JOIN videos v ON vc.video_id = v.video_id
                WHERE v.user_id = ?
                """,
                Long.class,
                creatorId
        );
        data.put("coins", coins != null ? coins : 0L);

        // 评论总量（不受时间范围影响）
        Long comments = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM comments c
                JOIN videos v ON c.video_id = v.video_id
                WHERE v.user_id = ?
                """,
                Long.class,
                creatorId
        );
        data.put("comments", comments != null ? comments : 0L);

        // 弹幕总量：该创作者全部视频在 Redis 中的弹幕总数
        List<String> creatorVideoIds = queryCreatorVideoIds(creatorId);
        long danmakuTotal = 0L;
        for (String videoId : creatorVideoIds) {
            danmakuTotal += danmakuService.getDanmakuCount(videoId);
        }
        data.put("danmaku", danmakuTotal);
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

        String[] dayKeys = new String[days];
        String[] labels = new String[days];
        for (int i = 0; i < days; i++) {
            LocalDate current = startDate.plusDays(i);
            dayKeys[i] = current.toString();
            labels[i] = current.format(dayFmt);
        }
        data.put("dayKeys", dayKeys);
        data.put("labels", labels);

        // 播放趋势：按天统计播放事件，确保与卡片总量同口径
        List<Map<String, Object>> playRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(vpe.played_at) AS d, COUNT(*) AS cnt
                FROM video_play_events vpe
                WHERE vpe.creator_id = ?
                  AND vpe.played_at BETWEEN ? AND ?
                GROUP BY DATE(vpe.played_at)
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

        // 粉丝播放：按天（使用 create_time 保证历史日值稳定）
        List<Map<String, Object>> fanPlayRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(ph.create_time) AS d, COUNT(*) AS cnt
                FROM play_history ph
                JOIN videos v ON ph.video_id = v.video_id
                JOIN fans f ON ph.user_id = f.follower_id AND f.following_id = v.user_id
                WHERE v.user_id = ?
                  AND ph.create_time BETWEEN ? AND ?
                GROUP BY DATE(ph.create_time)
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

        // 空间访客：每天访问主页的去重用户数
        List<Map<String, Object>> visitorRows = jdbcTemplate.queryForList(
                """
                SELECT pv.visit_date AS d, COUNT(DISTINCT pv.visitor_id) AS cnt
                FROM profile_visits pv
                WHERE pv.profile_user_id = ?
                  AND pv.visit_date BETWEEN DATE(?) AND DATE(?)
                GROUP BY pv.visit_date
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

        // 弹幕趋势：按 send_time 自然日聚合（Redis）
        List<String> creatorVideoIds = queryCreatorVideoIds(creatorId);
        Map<String, Long> danmakuMap = new HashMap<>();
        ZoneId zone = ZoneId.systemDefault();
        long startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli();
        long endMillis = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();
        for (String videoId : creatorVideoIds) {
            List<DanmakuDTO> all = danmakuService.listAllDanmaku(videoId);
            for (DanmakuDTO dto : all) {
                Long sendTime = dto.getSendTime();
                if (sendTime == null || sendTime < startMillis || sendTime >= endMillis) continue;
                LocalDate d = java.time.Instant.ofEpochMilli(sendTime).atZone(zone).toLocalDate();
                String dayKey = d.toString();
                danmakuMap.put(dayKey, danmakuMap.getOrDefault(dayKey, 0L) + 1L);
            }
        }
        long[] danmaku = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            danmaku[i] = danmakuMap.getOrDefault(d.toString(), 0L);
        }
        data.put("danmaku", danmaku);

        // 投币趋势（按天）
        List<Map<String, Object>> coinRows = jdbcTemplate.queryForList(
                """
                SELECT DATE(vc.create_time) AS d, COUNT(*) AS cnt
                FROM video_coins vc
                JOIN videos v ON vc.video_id = v.video_id
                WHERE v.user_id = ?
                  AND vc.create_time BETWEEN ? AND ?
                GROUP BY DATE(vc.create_time)
                """,
                creatorId, start, end
        );

        Map<String, Long> coinMap = new HashMap<>();
        for (Map<String, Object> row : coinRows) {
            String d = normalizeDayKey(row.get("d"));
            Object cnt = row.get("cnt");
            if (d != null && cnt != null) {
                coinMap.put(d, ((Number) cnt).longValue());
            }
        }

        long[] coinsTrend = new long[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            coinsTrend[i] = coinMap.getOrDefault(d.toString(), 0L);
        }
        data.put("coins", coinsTrend);

        // 分享：当前没有对应表结构，这里按 0 返回（保证前端可切换）
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

