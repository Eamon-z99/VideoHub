package videobackend.video.model;

public record ChatMessage(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        String createTime
) {
}


