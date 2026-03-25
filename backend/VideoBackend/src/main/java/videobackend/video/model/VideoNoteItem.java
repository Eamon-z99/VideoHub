package videobackend.video.model;

/**
 * 记笔记模型（用于返回给前端）
 */
public record VideoNoteItem(
        Long id,
        String videoId,
        String videoTitle,
        String videoCoverUrl,
        Long authorUserId,
        String authorUsername,
        String title,
        String content,
        Integer visibility,
        Integer status,
        String createdTime
) {
}

