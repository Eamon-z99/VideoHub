package videobackend.video.model;

/**
 * 播放历史记录项
 */
public class PlayHistoryItem {
    private Long id;
    private String videoId;
    private String title;
    private String coverUrl;
    private String videoUrl;
    private Integer playTime;  // 播放到的时间点（秒）
    private Integer duration;  // 视频总时长（秒）
    private Integer progressPercent;  // 观看进度百分比（0-100）
    private Boolean isWatched;  // 是否观看完成
    private Integer watchCount;  // 观看次数
    private String lastWatchTime;  // 最后观看时间
    private String createTime;  // 首次观看时间

    public PlayHistoryItem() {
    }

    public PlayHistoryItem(Long id, String videoId, String title, String coverUrl, String videoUrl,
                          Integer playTime, Integer duration, Integer progressPercent,
                          Boolean isWatched, Integer watchCount, String lastWatchTime, String createTime) {
        this.id = id;
        this.videoId = videoId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.videoUrl = videoUrl;
        this.playTime = playTime;
        this.duration = duration;
        this.progressPercent = progressPercent;
        this.isWatched = isWatched;
        this.watchCount = watchCount;
        this.lastWatchTime = lastWatchTime;
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

    public Integer getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Integer playTime) {
        this.playTime = playTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Boolean getIsWatched() {
        return isWatched;
    }

    public void setIsWatched(Boolean isWatched) {
        this.isWatched = isWatched;
    }

    public Integer getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(Integer watchCount) {
        this.watchCount = watchCount;
    }

    public String getLastWatchTime() {
        return lastWatchTime;
    }

    public void setLastWatchTime(String lastWatchTime) {
        this.lastWatchTime = lastWatchTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}

