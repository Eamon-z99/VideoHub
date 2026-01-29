package videobackend.video.model;

/**
 * 收藏项
 */
public class FavoriteItem {
    private Long id;
    private String videoId;
    private String title;
    private String coverUrl;
    private String videoUrl;
    private String duration;  // 视频时长（格式化字符串，如 "02:14"）
    private String createTime;  // 收藏时间

    public FavoriteItem() {
    }

    public FavoriteItem(Long id, String videoId, String title, String coverUrl, 
                       String videoUrl, String duration, String createTime) {
        this.id = id;
        this.videoId = videoId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.createTime = createTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}

