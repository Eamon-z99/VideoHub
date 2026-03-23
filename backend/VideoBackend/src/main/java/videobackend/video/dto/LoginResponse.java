package videobackend.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String loginAccount;
    /** 是否可访问管理端（普通用户登录恒为 false；管理端请用 /api/admin/auth/login） */
    private Boolean isAdmin;
}

