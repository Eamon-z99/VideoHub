package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.CreatorStatsService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/creator-stats")
@CrossOrigin(origins = "*")
public class CreatorStatsController {

    private final CreatorStatsService creatorStatsService;

    public CreatorStatsController(CreatorStatsService creatorStatsService) {
        this.creatorStatsService = creatorStatsService;
    }

    /**
     * 创作中心 - 数据中心总览
     *
     * 前端可以：
     * - 直接传 creatorId 查询指定 UP
     * - 不传 creatorId 时，使用当前登录用户（从 JWT 中解析）
     */
    @GetMapping("/overview")
    public ResponseEntity<?> overview(
            @RequestParam(value = "creatorId", required = false) Long creatorId,
            @RequestParam(value = "range", defaultValue = "7d") String range,
            Principal principal
    ) {
        try {
            Long finalCreatorId = creatorId;
            if (finalCreatorId == null) {
                // 兜底：如果没有显式传 creatorId，则尝试从 Principal 中解析
                // 这里约定 JwtInterceptor 已经把用户名放到 principal.getName() 中，
                // 实际项目可以替换成更稳定的用户 ID 解析方式。
                if (principal == null || principal.getName() == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "缺少 creatorId，且无法从登录态中解析用户"
                    ));
                }
                // 如果你的系统有 userService，可以根据 account / username 反查 ID；
                // 为避免引入更多依赖，这里直接返回错误，鼓励前端明确传 creatorId。
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "请在请求参数中显式传入 creatorId"
                ));
            }

            Map<String, Object> data = creatorStatsService.getOverview(finalCreatorId, range);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取创作数据失败: " + e.getMessage()
            ));
        }
    }
}

