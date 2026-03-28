package videobackend.video.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class UserLevelService {
    // 等级累计经验阈值：到达该阈值即升到对应等级
    // 0:0, 1:50, 2:200, 3:1500, 4:4500, 5:10800, 6:28800
    private static final int[] LEVEL_THRESHOLDS = new int[]{0, 50, 200, 1500, 4500, 10800, 28800};
    private static final int MAX_LEVEL = 6;
    private static final int MAX_EXP = LEVEL_THRESHOLDS[MAX_LEVEL];

    public static final String EXP_TYPE_LOGIN = "LOGIN";
    public static final String EXP_TYPE_WATCH = "WATCH";
    public static final String EXP_TYPE_COIN_GIFT = "COIN_GIFT";

    private final JdbcTemplate jdbcTemplate;

    public UserLevelService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getUserLevelProgress(Long userId) {
        Long expObj = jdbcTemplate.queryForObject(
                "SELECT COALESCE(exp, 0) FROM users WHERE id = ?",
                Long.class,
                userId
        );
        int exp = expObj != null ? expObj.intValue() : 0;
        exp = Math.max(0, Math.min(exp, MAX_EXP));

        int level = computeLevel(exp);
        int nextLevel = level >= MAX_LEVEL ? MAX_LEVEL : level + 1;

        int currentExpInLevel = exp - LEVEL_THRESHOLDS[level];
        int needExp = nextLevel == level ? 0 : (LEVEL_THRESHOLDS[nextLevel] - exp);

        double progressPercent = 0;
        int total = currentExpInLevel + needExp;
        if (total > 0) {
            progressPercent = currentExpInLevel * 100.0 / total;
        }

        return Map.of(
                "success", true,
                "level", level,
                "nextLevel", nextLevel,
                "currentExp", currentExpInLevel,
                "needExp", Math.max(0, needExp),
                "progressPercent", progressPercent
        );
    }

    private int computeLevel(int exp) {
        if (exp >= LEVEL_THRESHOLDS[6]) return 6;
        if (exp >= LEVEL_THRESHOLDS[5]) return 5;
        if (exp >= LEVEL_THRESHOLDS[4]) return 4;
        if (exp >= LEVEL_THRESHOLDS[3]) return 3;
        if (exp >= LEVEL_THRESHOLDS[2]) return 2;
        if (exp >= LEVEL_THRESHOLDS[1]) return 1;
        return 0;
    }

    @Transactional
    public void awardLoginExp(Long userId) {
        awardDailyExp(userId, EXP_TYPE_LOGIN, 5, 0);
    }

    @Transactional
    public void awardWatchExp(Long userId) {
        awardDailyExp(userId, EXP_TYPE_WATCH, 5, 0);
    }

    /**
     * 每次给“其他UP主”的视频投币，1币=10经验值；每日上限50经验值（即最多5次）。
     */
    @Transactional
    public void awardCoinGiftExp(Long userId, String videoId) {
        if (userId == null || videoId == null) return;

        // videos 可能不存在该 videoId（脏数据/已删除），queryForObject 会抛 expected 1 actual 0
        List<Long> creatorIds = jdbcTemplate.query(
                "SELECT user_id FROM videos WHERE video_id = ? LIMIT 1",
                (rs, rowNum) -> rs.getObject("user_id") == null ? null : rs.getLong("user_id"),
                videoId
        );
        Long creatorId = creatorIds.isEmpty() ? null : creatorIds.get(0);
        if (creatorId == null || creatorId.equals(userId)) {
            // 投给自己视频，不发放经验
            return;
        }

        // 每次投币请求发放 10 exp，日上限 50 exp（即最多 5 次）
        awardDailyExp(userId, EXP_TYPE_COIN_GIFT, 10, 1);
    }

    /**
     * @param dailyExpCap LOGIN/WATCH:5 ; COIN_GIFT:50
     * @param requestedExp 单次请求经验值（LOGIN/WATCH:5，COIN_GIFT:10）
     * @param requestedCoinCount 仅 COIN_GIFT 使用：单次投币次数（1=一次投币）
     */
    @Transactional
    protected void awardDailyExp(Long userId, String expType, int requestedExp, int requestedCoinCount) {
        if (userId == null) return;
        if (requestedExp <= 0) return;

        int dailyExpCap = switch (expType) {
            case EXP_TYPE_LOGIN, EXP_TYPE_WATCH -> 5;
            case EXP_TYPE_COIN_GIFT -> 50;
            default -> 0;
        };
        if (dailyExpCap <= 0) return;

        // 用户已达上限则不再发放
        Integer userExpObj = jdbcTemplate.queryForObject(
                "SELECT COALESCE(exp, 0) FROM users WHERE id = ?",
                Integer.class,
                userId
        );
        int currentExp = userExpObj != null ? userExpObj : 0;
        currentExp = Math.max(0, Math.min(currentExp, MAX_EXP));
        if (currentExp >= MAX_EXP) return;

        LocalDate today = LocalDate.now();
        String grantDate = today.toString(); // yyyy-MM-dd

        // 该行不存在时，queryForObject 会抛 expected 1 actual 0，所以用 queryForList 安全读取
        List<Integer> existingGrantedList = jdbcTemplate.query(
                "SELECT COALESCE(exp_granted, 0) AS v FROM user_exp_daily_grants WHERE user_id = ? AND grant_date = ? AND exp_type = ? LIMIT 1",
                (rs, rowNum) -> rs.getInt("v"),
                userId,
                grantDate,
                expType
        );
        int existingExpGranted = existingGrantedList.isEmpty() ? 0 : existingGrantedList.get(0);
        existingExpGranted = Math.max(0, existingExpGranted);

        int remainingDailyExp = dailyExpCap - existingExpGranted;
        if (remainingDailyExp <= 0) return;

        int grantExp;
        int grantCoinCount;
        if (EXP_TYPE_COIN_GIFT.equals(expType)) {
            // 按规则：每次投币必须完整 10exp；不足 10exp 不发
            if (remainingDailyExp < requestedExp) return;
            grantExp = requestedExp;
            grantCoinCount = requestedCoinCount;
        } else {
            grantExp = Math.min(requestedExp, remainingDailyExp);
            grantCoinCount = 0;
        }

        // 计算用户新经验并封顶到 MAX_EXP
        int newExp = Math.min(MAX_EXP, currentExp + grantExp);
        int actualGrantExp = newExp - currentExp;
        if (actualGrantExp <= 0) return;

        int newLevel = computeLevel(newExp);

        // 更新用户总经验/等级
        jdbcTemplate.update(
                "UPDATE users SET exp = ?, level = ? WHERE id = ?",
                newExp,
                newLevel,
                userId
        );

        // 更新每日发放记录
        int actualGrantCoins = (EXP_TYPE_COIN_GIFT.equals(expType) && actualGrantExp > 0) ? grantCoinCount : 0;

        jdbcTemplate.update(
                """
                INSERT INTO user_exp_daily_grants (user_id, grant_date, exp_type, exp_granted, coin_count, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                  exp_granted = exp_granted + VALUES(exp_granted),
                  coin_count = coin_count + VALUES(coin_count),
                  update_time = NOW()
                """,
                userId,
                grantDate,
                expType,
                actualGrantExp,
                actualGrantCoins
        );
    }
}

