package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.service.SearchHotKeywordService;
import videobackend.video.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db/search")
@CrossOrigin(origins = "*")
public class SearchHotKeywordController {

    private final SearchHotKeywordService searchHotKeywordService;
    private final JwtUtil jwtUtil;

    public SearchHotKeywordController(SearchHotKeywordService searchHotKeywordService, JwtUtil jwtUtil) {
        this.searchHotKeywordService = searchHotKeywordService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 记录一次搜索关键词（用于热搜统计）
     *
     * body: { keyword: string, weight?: number }
     */
    @PostMapping("/keyword-event")
    public ResponseEntity<?> keywordEvent(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        String keyword = body == null ? null : (String) body.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "keyword 不能为空"));
        }
        keyword = keyword.trim();

        int weight = 1;
        Object w = body.get("weight");
        if (w instanceof Number number) {
            weight = Math.max(1, number.intValue());
        }

        Long userId = getUserIdFromRequestIfValid(request);
        searchHotKeywordService.recordKeywordEvent(userId, keyword, weight);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 获取热搜关键词列表
     * 返回：[{ keyword, score, isNew, isHot }]（isHot 在非新词条中每次请求随机 3～5 条）
     */
    @GetMapping("/hot-keywords")
    public Map<String, Object> hotKeywords(@RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<Map<String, Object>> list = searchHotKeywordService.listHotKeywords(safeLimit);
        return Map.of(
                "list", list,
                "total", list.size()
        );
    }

    private Long getUserIdFromRequestIfValid(HttpServletRequest request) {
        if (request == null) return null;
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        String token = bearerToken.substring(7);
        Claims claims = jwtUtil.getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number num) {
            return num.longValue();
        }
        if (userIdObj instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }
}

