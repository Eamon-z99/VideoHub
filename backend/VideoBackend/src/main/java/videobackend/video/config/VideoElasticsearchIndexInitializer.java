package videobackend.video.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import videobackend.video.service.VideoElasticsearchService;

/**
 * 应用启动时把 MySQL 中的视频同步到 ES（可关闭以加快启动或避免阻塞）。
 */
@Component
@Order(2000)
@ConditionalOnProperty(prefix = "videohub.elasticsearch", name = "enabled", havingValue = "true")
@ConditionalOnBean(VideoElasticsearchService.class)
public class VideoElasticsearchIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(VideoElasticsearchIndexInitializer.class);

    private final VideoElasticsearchService videoElasticsearchService;

    @Value("${videohub.elasticsearch.reindex-on-startup:true}")
    private boolean reindexOnStartup;

    public VideoElasticsearchIndexInitializer(VideoElasticsearchService videoElasticsearchService) {
        this.videoElasticsearchService = videoElasticsearchService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!reindexOnStartup) {
            log.info("videohub.elasticsearch.reindex-on-startup=false, skip ES full reindex");
            return;
        }
        try {
            log.info("Starting Elasticsearch full reindex on startup...");
            long t0 = System.currentTimeMillis();
            videoElasticsearchService.fullReindex();
            log.info("Elasticsearch full reindex done in {} ms", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.error("Elasticsearch startup reindex failed; search may be empty until you fix ES and restart. {}", e.getMessage());
        }
    }
}
