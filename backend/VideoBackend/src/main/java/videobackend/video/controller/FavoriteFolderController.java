package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videobackend.video.model.FavoriteFolderItem;
import videobackend.video.service.FavoriteFolderService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite-folders")
@CrossOrigin(origins = "*")
public class FavoriteFolderController {

    private final FavoriteFolderService folderService;

    public FavoriteFolderController(FavoriteFolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam Long userId) {
        try {
            List<FavoriteFolderItem> list = folderService.listFolders(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "list", list,
                    "total", list.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取收藏夹失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String name = String.valueOf(request.get("name"));
            Boolean isPublic = request.get("isPublic") != null ? Boolean.valueOf(request.get("isPublic").toString()) : true;

            Long id = folderService.createFolder(userId, name, isPublic);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", id,
                    "message", "创建成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "创建失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/rename")
    public ResponseEntity<?> rename(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long folderId = Long.valueOf(request.get("folderId").toString());
            String name = String.valueOf(request.get("name"));

            boolean ok = folderService.renameFolder(userId, folderId, name);
            if (ok) {
                return ResponseEntity.ok(Map.of("success", true, "message", "重命名成功"));
            }
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "收藏夹不存在"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "重命名失败: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> delete(@PathVariable Long folderId, @RequestParam Long userId) {
        try {
            boolean ok = folderService.deleteFolder(userId, folderId);
            if (ok) {
                return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
            }
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "收藏夹不存在"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "删除失败: " + e.getMessage()
            ));
        }
    }
}


