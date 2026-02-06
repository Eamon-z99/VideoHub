package videobackend.video.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/feed-images")
public class ImageResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ImageResourceController.class);

    @Value("${feed.image.storage.root:E:\\\\FeedImages}")
    private String imageStorageRoot;

    @GetMapping("/**")
    public ResponseEntity<Resource> serveImage(HttpServletRequest request) {
        try {
            String requestPath = request.getRequestURI();
            
            String prefix = request.getContextPath() + "/feed-images";
            if (requestPath.startsWith(prefix)) {
                requestPath = requestPath.substring(prefix.length());
            } else if (requestPath.startsWith("/feed-images")) {
                requestPath = requestPath.substring("/feed-images".length());
            }

            if (requestPath.startsWith("/")) {
                requestPath = requestPath.substring(1);
            }

            // 添加 feed-images/ 前缀
            String filePathStr = requestPath.startsWith("feed-images/") ? requestPath : "feed-images/" + requestPath;
            Path filePath = Paths.get(imageStorageRoot, filePathStr).normalize();
            Path root = Paths.get(imageStorageRoot).normalize();

            if (!filePath.startsWith(root)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            String contentType = determineContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" +
                            UriUtils.encode(filePath.getFileName().toString(), StandardCharsets.UTF_8) + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("读取图片文件时发生异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String determineContentType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".webp")) {
            return "image/webp";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}

