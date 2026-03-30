package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import videobackend.video.model.VideoItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 首页 hero-grid 轮播位内容配置（管理员可控）。
 *
 * 表设计：home_hero_carousel(slot INT PRIMARY KEY, video_id VARCHAR(50))
 * - slot 按顺序对应前端 slides 的显示顺序
 * - 当配置为空时，回退为随机视频（复用 LocalVideoService.listPage 的 ORDER BY RAND）
 */
@Service
public class HomeHeroCarouselService {

    private static final String TABLE = "home_hero_carousel";
    private static final int DEFAULT_LIMIT = 6;

    private final JdbcTemplate jdbcTemplate;
    private final LocalVideoService localVideoService;

    public HomeHeroCarouselService(JdbcTemplate jdbcTemplate, LocalVideoService localVideoService) {
        this.jdbcTemplate = jdbcTemplate;
        this.localVideoService = localVideoService;
        ensureTableExists();
    }

    private void ensureTableExists() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `%s` (
                  `slot` INT NOT NULL,
                  `video_id` VARCHAR(50) NOT NULL,
                  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`slot`),
                  INDEX `idx_video_id` (`video_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """.formatted(TABLE));
    }

    /**
     * @return 配置的 videoIds，顺序即 slot 升序
     */
    public List<String> getConfiguredVideoIds() {
        List<SlotRow> rows = jdbcTemplate.query(
                "SELECT slot, video_id FROM `" + TABLE + "` ORDER BY slot ASC",
                (rs, i) -> new SlotRow(rs.getInt("slot"), rs.getString("video_id"))
        );
        rows.sort(Comparator.comparingInt(a -> a.slot));

        List<String> ids = new ArrayList<>();
        for (SlotRow row : rows) {
            if (StringUtils.hasText(row.videoId)) {
                ids.add(row.videoId);
            }
        }
        return ids;
    }

    public void setConfiguredVideoIds(List<String> videoIds) {
        // 清空再写入，避免残留 slot
        jdbcTemplate.update("DELETE FROM `" + TABLE + "`");
        if (videoIds == null || videoIds.isEmpty()) {
            return;
        }

        int slot = 0;
        for (String id : videoIds) {
            if (!StringUtils.hasText(id)) continue;
            String clean = id.trim();
            jdbcTemplate.update(
                    "INSERT INTO `" + TABLE + "` (slot, video_id) VALUES (?, ?)",
                    slot,
                    clean
            );
            slot++;
        }
    }

    /**
     * 给首页读取：优先返回配置内容；配置为空则回退随机（最大 limit 条）。
     */
    public List<VideoItem> getHeroVideosForHome(int limit) {
        int safeLimit = Math.max(1, Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, 20));

        List<String> configured = getConfiguredVideoIds();
        if (!configured.isEmpty()) {
            List<String> ids = configured.size() > safeLimit ? configured.subList(0, safeLimit) : configured;
            List<VideoItem> items = localVideoService.listByVideoIdsPreserveOrder(ids);
            // 若因状态过滤等原因取回为空，则继续回退随机
            if (items != null && !items.isEmpty()) {
                return items;
            }
        }

        // 回退随机：复用已有随机 SQL（LocalVideoService.listPage）
        return localVideoService.listPage(1, safeLimit);
    }

    private record SlotRow(int slot, String videoId) {}
}

