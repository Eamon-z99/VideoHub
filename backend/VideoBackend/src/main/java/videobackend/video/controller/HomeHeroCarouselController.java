package videobackend.video.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import videobackend.video.model.VideoItem;
import videobackend.video.service.HomeHeroCarouselService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db/home-hero")
@CrossOrigin(origins = "*")
public class HomeHeroCarouselController {

    private final HomeHeroCarouselService homeHeroCarouselService;

    public HomeHeroCarouselController(HomeHeroCarouselService homeHeroCarouselService) {
        this.homeHeroCarouselService = homeHeroCarouselService;
    }

    /**
     * 首页 hero-grid 使用的轮播内容：
     * - 管理员配置存在：按配置返回
     * - 未配置或取回为空：随机回退
     */
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<VideoItem> list = homeHeroCarouselService.getHeroVideosForHome(limit);
        return Map.of(
                "list", list,
                "total", list.size()
        );
    }
}

