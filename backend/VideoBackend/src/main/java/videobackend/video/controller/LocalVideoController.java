package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import videobackend.video.model.VideoItem;
import videobackend.video.service.LocalVideoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db/videos")
public class LocalVideoController {

    private final LocalVideoService localVideoService;

    public LocalVideoController(LocalVideoService localVideoService) {
        this.localVideoService = localVideoService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int pageSize) {
        List<VideoItem> items = localVideoService.listPage(page, pageSize);
        long total = localVideoService.count();
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

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoItem> detail(@PathVariable String videoId) {
        return localVideoService.findByVideoId(videoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


