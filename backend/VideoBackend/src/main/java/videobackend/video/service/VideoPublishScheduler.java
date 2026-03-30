package videobackend.video.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 定时发布：定时扫描到点投稿，并发布到 videos 表。
 *
 * 使用 MySQL GET_LOCK/RELEASE_LOCK 确保多实例时不会重复发布（锁基于连接/会话）。
 */
@Component
@ConditionalOnProperty(
        name = "video.publish.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class VideoPublishScheduler {

    private static final Logger log = LoggerFactory.getLogger(VideoPublishScheduler.class);

    // 多实例/多进程的互斥锁 key：不要与其它业务冲突
    private static final String LOCK_KEY = "videohub_publish_due_lock";
    private static final int LOCK_WAIT_SECONDS = 3;

    private final VideoSubmissionService videoSubmissionService;
    private final JdbcTemplate jdbcTemplate;

    public VideoPublishScheduler(VideoSubmissionService videoSubmissionService, JdbcTemplate jdbcTemplate) {
        this.videoSubmissionService = videoSubmissionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "${video.publish.scheduler.cron}") // 仅使用配置文件
    @Transactional
    public void publishDueScheduled() {
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

            // 逐条发布（publishDue 内部已做条件过滤）
            // operatorId 在当前实现中主要用于接口签名占位，可传 0L
            int limit = 200;
            var resp = videoSubmissionService.publishDue(limit, 0L);

            Number processedNum = (Number) resp.getOrDefault("processed", 0);
            Number publishedNum = (Number) resp.getOrDefault("published", 0);
            int processed = processedNum.intValue();
            int published = publishedNum.intValue();
            if (processed > 0 || published > 0) {
                log.info("publishDueScheduled: 扫描完成，processed={}, published={}", processed, published);
            }
        } catch (Exception e) {
            log.error("publishDueScheduled: 定时发布失败", e);
        } finally {
            try {
                if (locked != null && locked == 1) {
                    jdbcTemplate.queryForObject("SELECT RELEASE_LOCK(?)", Integer.class, LOCK_KEY);
                }
            } catch (Exception ignore) {
                // 尝试释放失败不影响业务主流程
            }
        }
    }
}

