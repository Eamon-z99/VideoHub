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
@RequestMapping("/avatars")
public class AvatarResourceController {

    private static final Logger logger = LoggerFactory.getLogger(AvatarResourceController.class);

    @Value("${avatar.storage.root:E:\\\\Avatars}")
    private String avatarStorageRoot;

    @GetMapping("/**")
    public ResponseEntity<Resource> serveAvatar(HttpServletRequest request) {
        try {
            String requestPath = request.getRequestURI();
            
            String prefix = request.getContextPath() + "/avatars";
            if (requestPath.startsWith(prefix)) {
                requestPath = requestPath.substring(prefix.length());
            } else if (requestPath.startsWith("/avatars")) {
                requestPath = requestPath.substring("/avatars".length());
            }

            if (requestPath.startsWith("/")) {
                requestPath = requestPath.substring(1);
            }

            // 添加 avatars/ 前缀，因为存储路径是 avatarStorageRoot/avatars/YYYY/MM/...
            // 而请求路径是 /avatars/YYYY/MM/...，需要加上 avatars/ 前缀才能正确读取
            String filePathStr = requestPath.startsWith("avatars/") ? requestPath : "avatars/" + requestPath;
            Path filePath = Paths.get(avatarStorageRoot, filePathStr).normalize();
            Path root = Paths.get(avatarStorageRoot).normalize();

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
            logger.error("读取头像文件时发生异常", e);
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
        }
        return "application/octet-stream";
    }
}


