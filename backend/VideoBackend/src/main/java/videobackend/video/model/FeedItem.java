package videobackend.video.model;

import java.util.List;

public record FeedItem(
        Long id,
        Long userId,
        String title,
        String content,
        List<String> images,
        Long likeCount,
        Long commentCount,
        Long shareCount,
        String createTime,
        String uploaderName,
        String uploaderAvatar,
        Long uploaderId
) {
}

