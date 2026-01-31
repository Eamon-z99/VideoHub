package videobackend.video.model;

/**
 * 收藏夹（返回给前端的简单模型）
 */
public class FavoriteFolderItem {
    private Long id;
    private Long userId;
    private String name;
    private Boolean isPublic;
    private String description; // 收藏夹描述
    private String coverUrl; // 封面URL（第一个收藏视频的封面）
    private Long count; // 该收藏夹内视频数
    private String createTime;

    public FavoriteFolderItem() {}

    public FavoriteFolderItem(Long id, Long userId, String name, Boolean isPublic, Long count, String createTime) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.isPublic = isPublic;
        this.count = count;
        this.createTime = createTime;
    }

    public FavoriteFolderItem(Long id, Long userId, String name, Boolean isPublic, String description, String coverUrl, Long count, String createTime) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.isPublic = isPublic;
        this.description = description;
        this.coverUrl = coverUrl;
        this.count = count;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}



