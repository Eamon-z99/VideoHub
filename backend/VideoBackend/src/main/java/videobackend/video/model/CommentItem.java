package videobackend.video.model;

/**
 * 视频评论模型
 */
public record CommentItem(
        Long id,
        String videoId,
        Long userId,
        String username,
        Integer level,
        String avatar,
        String content,
        Long parentId,
        Long replyToUserId,
        String replyToUsername,
        Long likeCount,
        String createTime
) {
}
