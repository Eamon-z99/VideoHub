package videobackend.video.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 热搜定时刷新：
 * - 每分钟统计最近 60 分钟内的搜索热度，写入 search_hot_keywords.score
 * - 清理过旧的事件数据，控制表大小
 */
@Component
public class SearchHotKeywordScheduler {

    private static final Logger log = LoggerFactory.getLogger(SearchHotKeywordScheduler.class);

    private static final String LOCK_KEY = "videohub_search_hot_lock";
    private static final int LOCK_WAIT_SECONDS = 2;

    private final JdbcTemplate jdbcTemplate;

    public SearchHotKeywordScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 */1 * * * *") // 每分钟
    public void refreshHotKeywords() {
        Integer locked = null;
        try {
            locked = jdbcTemplate.queryForObject(
                    "SELECT GET_LOCK(?, ?)",
                    Integer.class,
                    LOCK_KEY,
                    LOCK_WAIT_SECONDS
            );
            if (locked == null || locked != 1) {
                return;
            }

            // 最近 60 分钟聚合分数，并刷新到热搜表
            jdbcTemplate.update(
                    """
                    UPDATE search_hot_keywords hk
                    LEFT JOIN (
                      SELECT keyword,
                             SUM(weight) AS score,
                             MAX(create_time) AS last_seen_time
                      FROM search_keyword_events
                      WHERE create_time >= NOW() - INTERVAL 60 MINUTE
                      GROUP BY keyword
                    ) t ON hk.keyword = t.keyword
                    SET hk.score = COALESCE(t.score, 0),
                        hk.last_seen_time = COALESCE(t.last_seen_time, hk.last_seen_time)
                    """);

            // 清理 7 天前的事件数据（避免无限增长）
            jdbcTemplate.update(
                    "DELETE FROM search_keyword_events WHERE create_time < NOW() - INTERVAL 7 DAY"
            );
        } catch (Exception e) {
            log.error("refreshHotKeywords failed", e);
        } finally {
            try {
                if (locked != null && locked == 1) {
                    jdbcTemplate.queryForObject("SELECT RELEASE_LOCK(?)", Integer.class, LOCK_KEY);
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
    }
}

