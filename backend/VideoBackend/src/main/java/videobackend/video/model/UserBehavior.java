package videobackend.video.model;

import java.time.LocalDateTime;

/**
 * 用户行为数据模型
 * 用于推荐系统的用户行为分析
 */
public record UserBehavior(
        Long userId,
        String videoId,
        BehaviorType type,
        Double score,  // 行为评分：点赞=1.0, 收藏=0.8, 播放>50%=0.6, 播放>25%=0.4, 播放>0=0.2
        LocalDateTime timestamp
) {
    public enum BehaviorType {
        LIKE,       // 点赞
        FAVORITE,   // 收藏
        WATCH,      // 观看
        COMMENT     // 评论
    }
}



