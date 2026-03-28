package videobackend.video.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import videobackend.video.dto.VideoSearchRequest;
import videobackend.video.model.VideoItem;
import videobackend.video.service.LocalVideoService;
import videobackend.video.service.VideoSubmissionService;
import videobackend.video.util.JwtUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/db/videos")
public class LocalVideoController {

    private final LocalVideoService localVideoService;
    private final VideoSubmissionService videoSubmissionService;
    private final JwtUtil jwtUtil;

    public LocalVideoController(LocalVideoService localVideoService,
                                VideoSubmissionService videoSubmissionService,
                                JwtUtil jwtUtil) {
        this.localVideoService = localVideoService;
        this.videoSubmissionService = videoSubmissionService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int pageSize,
                                    @RequestParam(required = false) Long userId,
                                    @RequestParam(required = false) Boolean followingOnly,
                                    @RequestParam(required = false) Long followingId,
                                    @RequestParam(required = false) String tag,
                                    @RequestParam(required = false) String partitionTag) {
        List<VideoItem> items;
        long total;

        if (followingOnly != null && followingOnly && userId != null) {
            items = localVideoService.listPageByFollowing(userId, followingId, page, pageSize);
            total = localVideoService.countByFollowing(userId, followingId);
        } else if (StringUtils.hasText(partitionTag)) {
            items = localVideoService.listPageByPartitionLabel(partitionTag.trim(), page, pageSize);
            total = localVideoService.countByPartitionLabel(partitionTag.trim());
        } else if (StringUtils.hasText(tag)) {
            items = localVideoService.listPageByTag(tag.trim(), page, pageSize);
            total = localVideoService.countByTag(tag.trim());
        } else {
            items = localVideoService.listPage(page, pageSize);
            total = localVideoService.count();
        }
        
        return Map.of(
                "list", items,
                "page", page,
                "pageSize", pageSize,
                "total", total
        );
    }

    /**
     * 用户上传视频
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(HttpServletRequest request,
                                    @RequestPart("file") MultipartFile file,
                                    @RequestPart(value = "cover", required = false) MultipartFile cover,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false, name = "duration") Integer durationSeconds,
                                    @RequestParam(required = false, name = "copyright") String copyright,
                                    @RequestParam(required = false, name = "partition") String partition,
                                    @RequestParam(required = false, name = "tags") String tagsJson,
                                    @RequestParam(required = false) String scheduleEnabled,
                                    @RequestParam(required = false) String schedulePublishAt,
                                    @RequestParam(required = false) String collectionEnabled,
                                    @RequestParam(required = false) String collectionName,
                                    @RequestParam(required = false) String allowSecondCreation,
                                    @RequestParam(required = false) String commercialPromotion,
                                    @RequestParam(required = false, defaultValue = "0") int draftOnly) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "未登录或登录已过期"));
            }

            String submissionId = videoSubmissionService.createSubmission(
                    userId,
                    title,
                    description,
                    file,
                    cover,
                    durationSeconds,
                    copyright,
                    partition,
                    tagsJson,
                    scheduleEnabled,
                    schedulePublishAt,
                    collectionEnabled,
                    collectionName,
                    allowSecondCreation,
                    commercialPromotion,
                    draftOnly == 1
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "submissionId", submissionId,
                    "reviewStatus", "DRAFT"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "视频上传失败，请稍后重试"));
        }
    }

    /**
     * 按 JSON 请求体搜索视频：{ "keyword": "...", "page": 1, "pageSize": 20 }
     */
    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody VideoSearchRequest req) {
        int page = req.getPage() <= 0 ? 1 : req.getPage();
        int pageSize = req.getPageSize() <= 0 ? 20 : req.getPageSize();
        String keyword = req.getKeyword();

        List<VideoItem> items = localVideoService.listPageByKeyword(keyword, page, pageSize);
        long total = localVideoService.countByKeyword(keyword);

        return Map.of(
                "list", items,
                "page", page,
                "pageSize", pageSize,
                "total", total
        );
    }

    @GetMapping("/top")
    public Map<String, Object> top(@RequestParam(defaultValue = "6") int limit) {
        List<VideoItem> items = localVideoService.listTopByViewCount(limit);
        return Map.of(
                "list", items,
                "total", items.size()
        );
    }

    @GetMapping("/by-uploader")
    public Map<String, Object> listByUploader(@RequestParam Long uploaderId,
                                              @RequestParam(defaultValue = "4") int limit,
                                              @RequestParam(required = false) String excludeVideoId) {
        List<VideoItem> items = localVideoService.listByUploader(uploaderId, excludeVideoId, limit);
        return Map.of(
                "list", items,
                "total", items.size()
        );
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoItem> detail(HttpServletRequest request, @PathVariable String videoId) {
        // 对于下架视频（FAILED/DOWN）：仅作者本人可查看；其他人一律 404
        Long requesterUserId = getUserIdFromRequestIfValid(request);
        var accessOpt = localVideoService.getVideoAccessInfo(videoId);
        if (accessOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LocalVideoService.VideoAccessInfo access = accessOpt.get();
        String st = access.status() == null ? "" : access.status().trim().toUpperCase(Locale.ROOT);
        boolean isTakedown = "FAILED".equals(st) || "DOWN".equals(st);
        if (isTakedown) {
            if (requesterUserId == null || access.ownerUserId() == null || !requesterUserId.equals(access.ownerUserId())) {
                return ResponseEntity.notFound().build();
            }
            return localVideoService.findByVideoIdIncludingTakedown(videoId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        return localVideoService.findByVideoId(videoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                }
                if (userIdObj instanceof String) {
                    try {
                        return Long.parseLong((String) userIdObj);
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return null;
    }

    private Long getUserIdFromRequestIfValid(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        String token = bearerToken.substring(7);
        if (token.isBlank() || !jwtUtil.validateToken(token)) {
            return null;
        }
        Claims claims = jwtUtil.getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }
}


