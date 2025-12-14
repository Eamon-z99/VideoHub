package videobackend.video.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String account;
    private String password;
    private String email;
    private String avatar;
    private String bio;
    private Integer status; // 0-禁用，1-正常
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

