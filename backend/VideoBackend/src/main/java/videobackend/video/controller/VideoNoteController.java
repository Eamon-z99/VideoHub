package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.VideoNoteItem;
import videobackend.video.service.VideoNoteService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video-notes")
@CrossOrigin(origins = "*")
public class VideoNoteController {

    private final VideoNoteService videoNoteService;
    private final JwtUtil jwtUtil;

    public VideoNoteController(VideoNoteService videoNoteService, JwtUtil jwtUtil) {
        this.videoNoteService = videoNoteService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户创建笔记
     * body: { videoId, title?, content, visibility }
     */
    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }

        Object videoIdObj = body.get("videoId");
        Object contentObj = body.get("content");
        Object visibilityObj = body.get("visibility");
        Object titleObj = body.get("title");

        if (videoIdObj == null || contentObj == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少 videoId 或 content"));
        }

        String videoId = videoIdObj.toString();
        String content = contentObj.toString();
        Integer visibility = visibilityObj == null ? 1 : (visibilityObj instanceof Number n ? n.intValue() : Integer.parseInt(visibilityObj.toString()));
        String title = titleObj == null ? null : titleObj.toString();

        long noteId = videoNoteService.createNote(userId, videoId, title, content, visibility);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("noteId", noteId)));
    }

    /**
     * 获取当前用户在某个作品下的历史笔记（用于“返回查看历史”）
     * GET /api/video-notes/history?videoId=xxx
     */
    @GetMapping("/history")
    public ResponseEntity<?> history(HttpServletRequest request, @RequestParam String videoId) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        List<VideoNoteItem> list = videoNoteService.listNotesByVideoAndAuthor(userId, videoId);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "list", list
        )));
    }

    /**
     * 获取当前用户下的全部笔记（跨视频）
     * GET /api/video-notes/history/all
     */
    @GetMapping("/history/all")
    public ResponseEntity<?> historyAll(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        List<VideoNoteItem> list = videoNoteService.listAllNotesByAuthor(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "list", list
        )));
    }

    /**
     * 获取单条笔记详情（用于详情预览）
     * GET /api/video-notes/{noteId}
     */
    @GetMapping("/{noteId}")
    public ResponseEntity<?> getOne(HttpServletRequest request, @PathVariable Long noteId) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }
        VideoNoteItem item = videoNoteService.getNoteById(noteId);
        if (item == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "笔记不存在"));
        }
        // 仅允许作者查看（简化权限）
        if (!item.authorUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", item));
    }

    /**
     * 更新笔记内容（仅更新 content）
     * PUT /api/video-notes/{noteId}
     * body: { content }
     */
    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateContent(
            HttpServletRequest request,
            @PathVariable Long noteId,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }

        Object contentObj = body.get("content");
        if (contentObj == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少 content"));
        }

        VideoNoteItem item = videoNoteService.getNoteById(noteId);
        if (item == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "笔记不存在"));
        }
        if (!item.authorUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "无权限"));
        }

        String content = contentObj.toString();
        try {
            int updated = videoNoteService.updateNoteContent(userId, noteId, content);
            if (updated <= 0) {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "笔记更新失败"));
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

}

