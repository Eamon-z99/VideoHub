package videobackend.video.controller;

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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/local-videos")
public class LocalVideoResourceController {

    @Value("${media.storage.root}")
    private String mediaStorageRoot;

    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 获取请求路径，去掉 /local-videos 前缀
            String requestPath = request.getRequestURI();
            String prefix = request.getContextPath() + "/local-videos";
            if (requestPath.startsWith(prefix)) {
                requestPath = requestPath.substring(prefix.length());
            } else if (requestPath.startsWith("/local-videos")) {
                requestPath = requestPath.substring("/local-videos".length());
            }
            
            // 移除开头的斜杠
            if (requestPath.startsWith("/")) {
                requestPath = requestPath.substring(1);
            }
            
            // URL解码路径段
            String[] segments = requestPath.split("/");
            StringBuilder decodedPath = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                if (i > 0) {
                    decodedPath.append("/");
                }
                try {
                    // 对每个路径段进行解码
                    String decoded = UriUtils.decode(segments[i], StandardCharsets.UTF_8);
                    decodedPath.append(decoded);
                } catch (IllegalArgumentException e) {
                    // 如果解码失败，使用原始值
                    decodedPath.append(segments[i]);
                }
            }
            
            // 构建文件路径
            Path filePath = Paths.get(mediaStorageRoot, decodedPath.toString()).normalize();
            Path mediaRoot = Paths.get(mediaStorageRoot).normalize();
            
            // 安全检查：确保路径在 mediaRoot 内
            if (!filePath.startsWith(mediaRoot)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 检查文件是否存在
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                // 如果文件不存在，尝试在目录中查找匹配的文件（处理文件名编码差异）
                Path parentDir = filePath.getParent();
                String targetFileName = filePath.getFileName().toString();
                
                if (parentDir != null && Files.isDirectory(parentDir)) {
                    Path foundFile = findFileInDirectory(parentDir, targetFileName);
                    if (foundFile != null) {
                        filePath = foundFile;
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
            
            Resource resource = new FileSystemResource(filePath);
            
            // 确定Content-Type
            String contentType = determineContentType(filePath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + 
                            UriUtils.encode(filePath.getFileName().toString(), StandardCharsets.UTF_8) + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 在目录中查找匹配的文件（处理文件名编码差异，特别是包含%的情况）
     */
    private Path findFileInDirectory(Path dir, String targetFileName) {
        try {
            // 首先尝试精确匹配
            Path exactMatch = dir.resolve(targetFileName);
            if (Files.exists(exactMatch) && Files.isRegularFile(exactMatch)) {
                return exactMatch;
            }
            
            // 在目录中查找匹配的文件
            try (var stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        String fileName = file.getFileName().toString();
                        
                        // 1. 精确匹配（包括包含%的文件名）
                        if (fileName.equals(targetFileName)) {
                            return file;
                        }
                        
                        // 2. 忽略大小写匹配
                        if (fileName.equalsIgnoreCase(targetFileName)) {
                            return file;
                        }
                        
                        // 3. Unicode规范化匹配（保留%字符）
                        String normalizedTarget = normalizeForCompare(targetFileName);
                        String normalizedFile = normalizeForCompare(fileName);
                        if (normalizedTarget.equals(normalizedFile)) {
                            return file;
                        }
                        
                        // 4. 对于包含%的文件名，尝试移除%后比较（作为最后的备选方案）
                        String targetWithoutPercent = targetFileName.replace("%", "").replace("％", "");
                        String fileWithoutPercent = fileName.replace("%", "").replace("％", "");
                        if (targetWithoutPercent.equalsIgnoreCase(fileWithoutPercent) && 
                            targetFileName.contains("%") == fileName.contains("%")) {
                            // 只有在都包含%或都不包含%时才匹配
                            return file;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    
    /**
     * 规范化文件名用于比较（处理全角/半角、Unicode规范化等，但保留%字符）
     */
    private String normalizeForCompare(String fileName) {
        // Unicode规范化
        String normalized = java.text.Normalizer.normalize(fileName, java.text.Normalizer.Form.NFKC);
        // 统一全角/半角标点（但保留%）
        normalized = normalized.replace('！', '!')
                .replace('？', '?')
                .replace('％', '%')  // 全角%转为半角%，但保留%
                .replace('—', '-')
                .replace('–', '-')
                .replace('－', '-');
        return normalized.toLowerCase();
    }
    
    /**
     * 根据文件扩展名确定Content-Type
     */
    private String determineContentType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".mkv")) {
            return "video/x-matroska";
        } else if (fileName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
}

