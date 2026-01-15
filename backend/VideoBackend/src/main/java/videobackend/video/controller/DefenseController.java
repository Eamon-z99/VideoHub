package videobackend.video.controller;

import org.springframework.web.bind.annotation.*;
import videobackend.video.service.DefenseService;

import java.util.Map;
import java.util.Set;

/**
 * 防御系统控制器
 */
@RestController
@RequestMapping("/api/defense")
public class DefenseController {

    private final DefenseService defenseService;

    public DefenseController(DefenseService defenseService) {
        this.defenseService = defenseService;
    }

    /**
     * 检测指定用户是否为攻击者
     */
    @GetMapping("/detect/{userId}")
    public DefenseService.DefenseResult detectUser(@PathVariable Long userId) {
        return defenseService.detectAttack(userId);
    }

    /**
     * 获取所有可疑用户列表
     */
    @GetMapping("/suspicious")
    public Map<String, Object> getSuspiciousUsers() {
        Set<Long> suspiciousUsers = defenseService.getSuspiciousUsers();
        return Map.of(
                "suspiciousUsers", suspiciousUsers,
                "count", suspiciousUsers.size()
        );
    }
}



