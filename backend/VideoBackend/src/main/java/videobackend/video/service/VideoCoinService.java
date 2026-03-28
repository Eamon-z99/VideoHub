package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VideoCoinService {

    private final JdbcTemplate jdbcTemplate;
    private final UserLevelService userLevelService;

    public VideoCoinService(JdbcTemplate jdbcTemplate, UserLevelService userLevelService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userLevelService = userLevelService;
    }

    @Transactional
    public boolean coinVideo(Long userId, String videoId) {
        if (userId == null || videoId == null) {
            throw new IllegalArgumentException("用户ID和视频ID不能为空");
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM video_coins WHERE user_id = ? AND video_id = ?",
                Integer.class, userId, videoId
        );
        if (count != null && count > 0) {
            return false;
        }

        // 扣减用户硬币余额（余额不足时不允许投币）
        int deducted = jdbcTemplate.update(
                "UPDATE users SET coin_balance = coin_balance - 1 WHERE id = ? AND coin_balance > 0",
                userId
        );
        if (deducted <= 0) {
            throw new IllegalStateException("硬币不足，无法投币");
        }

        int inserted = jdbcTemplate.update(
                "INSERT INTO video_coins (user_id, video_id, create_time) VALUES (?, ?, NOW())",
                userId, videoId
        );
        if (inserted > 0) {
            // 规则：每日给其他UP主的视频投币获得经验（1币=10经验，日上限50）
            userLevelService.awardCoinGiftExp(userId, videoId);
        }

        return inserted > 0;
    }

    @Transactional
    public boolean uncoinVideo(Long userId, String videoId) {
        if (userId == null || videoId == null) {
            throw new IllegalArgumentException("用户ID和视频ID不能为空");
        }
        int deleted = jdbcTemplate.update(
                "DELETE FROM video_coins WHERE user_id = ? AND video_id = ?",
                userId, videoId
        );
        if (deleted > 0) {
            // 取消投币后返还 1 枚硬币
            jdbcTemplate.update("UPDATE users SET coin_balance = coin_balance + 1 WHERE id = ?", userId);
        }
        return deleted > 0;
    }

    public boolean isCoined(Long userId, String videoId) {
        if (userId == null || videoId == null) return false;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM video_coins WHERE user_id = ? AND video_id = ?",
                Integer.class, userId, videoId
        );
        return count != null && count > 0;
    }

    public long getCoinCount(String videoId) {
        if (videoId == null) return 0L;
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM video_coins WHERE video_id = ?",
                Long.class, videoId
        );
        return count != null ? count : 0L;
    }

    public long getUserCoinBalance(Long userId) {
        if (userId == null) return 0L;
        Long balance = jdbcTemplate.queryForObject(
                "SELECT COALESCE(coin_balance, 0) FROM users WHERE id = ?",
                Long.class, userId
        );
        return balance != null ? balance : 0L;
    }
}

