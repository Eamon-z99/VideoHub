package videobackend.video.dto;

/**
 * 弹幕传输对象
 */
public class DanmakuDTO {
    /**
     * 发送弹幕的用户ID（从 JWT 中解析）
     */
    private Long userId;

    /**
     * 视频内时间（秒）
     */
    private Double time;

    /**
     * 弹幕内容
     */
    private String content;

    /**
     * 弹幕颜色（前端可传如 #ffffff）
     */
    private String color;

    /**
     * 弹幕模式（滚动 scroll / 顶部 top / 底部 bottom）
     */
    private String mode;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}


