package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.dto.DanmakuDTO;
import videobackend.video.service.DanmakuService;
import videobackend.video.util.JwtUtil;

import java.time.LocalDate;
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

    /**
     * 分页获取弹幕列表（所有用户可访问）
     * GET /api/danmaku/{videoId}/list?page=1&pageSize=20
     */
    @GetMapping("/{videoId}/list")
    public ResponseEntity<?> getDanmakuList(@PathVariable("videoId") String videoId,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        try {
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20;
            
            List<DanmakuDTO> list = danmakuService.listDanmakuByPage(videoId, page, pageSize);
            long total = danmakuService.getDanmakuCount(videoId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "list", list,
                    "total", total,
                    "page", page,
                    "pageSize", pageSize
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "获取弹幕列表失败"));
        }
    }

    /**
     * 获取某视频全部弹幕（不分页，一次性返回），供右侧弹幕列表使用
     * GET /api/danmaku/{videoId}/all
     */
    @GetMapping("/{videoId}/all")
    public ResponseEntity<?> getAllDanmaku(@PathVariable("videoId") String videoId) {
        try {
            List<DanmakuDTO> list = danmakuService.listAllDanmaku(videoId);
            long total = list.size();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "list", list,
                    "total", total
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "获取全部弹幕失败"));
        }
    }

    /**
     * 按发送日期获取某视频的弹幕列表（不分页）
     * GET /api/danmaku/{videoId}/date?date=2026-03-14
     */
    @GetMapping("/{videoId}/date")
    public ResponseEntity<?> getDanmakuByDate(@PathVariable("videoId") String videoId,
                                              @RequestParam("date")
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                              LocalDate date) {
        try {
            List<DanmakuDTO> list = danmakuService.listDanmakuByDate(videoId, date);
            long total = list.size();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "list", list,
                    "total", total
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "按日期获取弹幕失败"));
        }
    }
}


