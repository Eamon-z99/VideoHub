package videobackend.video.model;

public record VideoItem(
        String videoId,
        String title,
        String description,
        Integer duration,
        String coverUrl,
        String playUrl,
        String storagePath,
        String sourceFile,
        Long viewCount,
        Long fileSize,
        String uploaderName,
        String uploadDate
) {
}


