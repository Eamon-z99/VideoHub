package videobackend.video.model;

/**
 * 视频评论模型
 */
public record CommentItem(
        Long id,
        String videoId,
        Long userId,
        String username,
        String avatar,
        String content,
        Long parentId,
        Long likeCount,
        String createTime
) {
}


