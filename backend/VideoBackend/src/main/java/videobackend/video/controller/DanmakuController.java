package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.dto.DanmakuDTO;
import videobackend.video.service.DanmakuService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

/**
 * 弹幕相关接口：
 * - POST /api/danmaku?videoId=xxx    发送弹幕（需要登录）
 * - GET  /api/danmaku/{videoId}?from=&to=   按时间段获取弹幕（无需登录）
 */
@RestController
@RequestMapping("/api/danmaku")
public class DanmakuController {

    private final DanmakuService danmakuService;
    private final JwtUtil jwtUtil;

    public DanmakuController(DanmakuService danmakuService, JwtUtil jwtUtil) {
        this.danmakuService = danmakuService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 发送弹幕（需要登录）
     */
    @PostMapping
    public ResponseEntity<?> sendDanmaku(@RequestParam("videoId") String videoId,
                                         @RequestBody DanmakuDTO dto,
                                         HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            dto.setUserId(userId);
            danmakuService.addDanmaku(videoId, dto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (DataAccessException e) {
            // 典型：Redis 连接失败、Redis key 类型错误等
            e.printStackTrace();
            return ResponseEntity.status(503)
                    .body(Map.of(
                            "success", false,
                            "message", "弹幕服务暂不可用（Redis 访问失败）",
                            "error", e.getClass().getSimpleName()
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "success", false,
                            "message", "发送弹幕失败",
                            "error", e.getClass().getSimpleName()
                    ));
        }
    }

    /**
     * 按时间范围获取弹幕（所有用户可访问）
     */
    @GetMapping("/{videoId}")
    public ResponseEntity<?> getDanmaku(@PathVariable("videoId") String videoId,
                                        @RequestParam("from") double from,
                                        @RequestParam("to") double to) {
        List<DanmakuDTO> list = danmakuService.listDanmaku(videoId, from, to);
        return ResponseEntity.ok(Map.of("success", true, "list", list));
    }

    /**
     * 获取视频的弹幕总数（所有用户可访问）
     */
    @GetMapping("/{videoId}/count")
    public ResponseEntity<?> getDanmakuCount(@PathVariable("videoId") String videoId) {
        try {
            long count = danmakuService.getDanmakuCount(videoId);
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "获取弹幕总数失败"));
        }
    }
}


