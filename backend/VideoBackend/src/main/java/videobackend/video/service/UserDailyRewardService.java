package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserDailyRewardService {

    private final JdbcTemplate jdbcTemplate;

    public UserDailyRewardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 每日登录奖励：+1 硬币（一天一次）
     */
    @Transactional
    public void awardDailyLoginCoin(Long userId) {
        if (userId == null) return;

        String grantDate = LocalDate.now().toString(); // yyyy-MM-dd

        List<Integer> existed = jdbcTemplate.query(
                "SELECT 1 AS v FROM user_daily_login_coin_grants WHERE user_id = ? AND grant_date = ? LIMIT 1",
                (rs, rowNum) -> rs.getInt("v"),
                userId,
                grantDate
        );
        if (!existed.isEmpty()) return;

        jdbcTemplate.update(
                "INSERT INTO user_daily_login_coin_grants (user_id, grant_date, coin_granted, create_time) VALUES (?, ?, 1, NOW())",
                userId,
                grantDate
        );
        jdbcTemplate.update(
                "UPDATE users SET coin_balance = COALESCE(coin_balance, 0) + 1 WHERE id = ?",
                userId
        );
    }
}

