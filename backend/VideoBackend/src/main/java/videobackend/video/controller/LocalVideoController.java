package videobackend.video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import videobackend.video.model.VideoItem;
import videobackend.video.service.LocalVideoService;

import java.util.List;

@RestController
@RequestMapping("/api/db/videos")
public class LocalVideoController {

    private final LocalVideoService localVideoService;

    public LocalVideoController(LocalVideoService localVideoService) {
        this.localVideoService = localVideoService;
    }

    @GetMapping
    public List<VideoItem> list() {
        return localVideoService.listAll();
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoItem> detail(@PathVariable String videoId) {
        return localVideoService.findByVideoId(videoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
