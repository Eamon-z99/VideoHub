package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.FavoriteItem;
import videobackend.video.service.FavoriteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String videoId = request.get("videoId").toString();
            Long folderId = request.get("folderId") != null ? Long.valueOf(request.get("folderId").toString()) : null;

            Map<String, Object> result = favoriteService.addFavorite(userId, videoId, folderId);
            boolean created = Boolean.TRUE.equals(result.get("created"));
            boolean moved = Boolean.TRUE.equals(result.get("moved"));
            if (created) {
                return ResponseEntity.ok(Map.of("success", true, "message", "收藏成功", "result", result));
            }
            if (moved) {
                return ResponseEntity.ok(Map.of("success", true, "message", "已移动到新的收藏夹", "result", result));
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "已收藏过该视频", "result", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "收藏失败: " + e.getMessage()));
        }
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    public ResponseEntity<?> removeFavorite(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String videoId = request.get("videoId").toString();

            boolean success = favoriteService.removeFavorite(userId, videoId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "取消收藏成功"));
            } else {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "未收藏该视频"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "取消收藏失败: " + e.getMessage()));
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkFavorite(
            @RequestParam Long userId,
            @RequestParam String videoId) {
        try {
            boolean isFavorited = favoriteService.isFavorited(userId, videoId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isFavorited", isFavorited
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getFavoriteList(
            @RequestParam Long userId,
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<FavoriteItem> list = favoriteService.getUserFavorites(userId, folderId, page, pageSize);
            Long total = favoriteService.getUserFavoriteCount(userId, folderId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "list", list,
                "page", page,
                "pageSize", pageSize,
                "total", total,
                "folderId", folderId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "获取失败: " + e.getMessage()));
        }
    }

    /**
     * 删除单条收藏记录
     */
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<?> deleteFavorite(
            @PathVariable Long favoriteId,
            @RequestParam Long userId) {
        try {
            boolean deleted = favoriteService.deleteFavorite(userId, favoriteId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
            } else {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "收藏记录不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "删除失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除收藏记录
     */
    @PostMapping("/batch-delete")
    public ResponseEntity<?> batchDeleteFavorites(
            @RequestParam Long userId,
            @RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> favoriteIds = request.get("ids");
            int deleted = favoriteService.batchDeleteFavorites(userId, favoriteIds);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "删除成功",
                "deletedCount", deleted
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "批量删除失败: " + e.getMessage()));
        }
    }
}

