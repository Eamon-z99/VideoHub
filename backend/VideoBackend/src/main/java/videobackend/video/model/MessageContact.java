package videobackend.video.model;

public record MessageContact(
        Long userId,
        String username,
        String avatar,
        String lastContent,
        String lastTime,
        Long unreadCount
) {
}


