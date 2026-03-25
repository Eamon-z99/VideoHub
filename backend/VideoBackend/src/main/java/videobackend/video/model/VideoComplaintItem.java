package videobackend.video.model;

/**
 * 稿件举报模型
 */
public record VideoComplaintItem(
        Long id,
        String videoId,
        String videoTitle,
        Long reporterUserId,
        String reporterUsername,
        String category,
        String otherDetail,
        String evidenceUrls,
        Integer status,
        Long handlerAdminId,
        String handlerAction,
        String handlerNote,
        String createdTime
) {
}

