package videobackend.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.VideoComplaintItem;
import videobackend.video.service.VideoComplaintService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video-complaints")
@CrossOrigin(origins = "*")
public class VideoComplaintController {

    private final VideoComplaintService videoComplaintService;
    private final JwtUtil jwtUtil;

    public VideoComplaintController(VideoComplaintService videoComplaintService, JwtUtil jwtUtil) {
        this.videoComplaintService = videoComplaintService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户提交稿件举报
     * body: { videoId, category, detail, evidenceUrls? }
     */
    @PostMapping
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
        }

        Object videoIdObj = body.get("videoId");
        Object categoryObj = body.get("category");
        // 前端可能传 detail 或其它字段名：兼容写法
        Object detailObj = body.containsKey("detail") ? body.get("detail") : body.get("otherDetail");
        Object evidenceUrlsObj = body.get("evidenceUrls");

        if (videoIdObj == null || categoryObj == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "缺少 videoId 或 category"));
        }

        String videoId = videoIdObj.toString();
        String category = categoryObj.toString();
        String detail = detailObj == null ? null : detailObj.toString();
        String evidenceUrlsJson = toJsonArrayString(evidenceUrlsObj);

        long id = videoComplaintService.createComplaint(userId, videoId, category, detail, evidenceUrlsJson);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("complaintId", id)));
    }

    /**
     * 把 evidenceUrls 转成 JSON 数组字符串（便于管理端直接 parse 展示）。
     */
    private String toJsonArrayString(Object evidenceUrlsObj) {
        if (evidenceUrlsObj == null) return null;
        if (evidenceUrlsObj instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) return null;
            // 如果前端已传 JSON 数组字符串，直接复用
            if (t.startsWith("[") && t.endsWith("]")) return t;
            // 否则当作单个 url
            return "[" + "\"" + escapeJson(t) + "\"" + "]";
        }
        if (evidenceUrlsObj instanceof List<?> list) {
            if (list.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < list.size(); i++) {
                Object v = list.get(i);
                if (v == null) continue;
                String url = String.valueOf(v).trim();
                if (url.isEmpty()) continue;
                if (sb.length() > 1) sb.append(',');
                sb.append('"').append(escapeJson(url)).append('"');
            }
            sb.append(']');
            String out = sb.toString();
            return out.equals("[]") ? null : out;
        }
        // 兜底：转字符串当作单 url
        String t = String.valueOf(evidenceUrlsObj).trim();
        return t.isEmpty() ? null : "[" + "\"" + escapeJson(t) + "\"" + "]";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}

