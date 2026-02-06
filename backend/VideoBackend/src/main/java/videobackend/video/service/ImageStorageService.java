package videobackend.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${feed.image.storage.root:E:\\\\FeedImages}")
    private String imageStorageRoot;

    @Value("${media.cdn.enabled:false}")
    private boolean useCdn;

    @Value("${media.cdn.base-url:}")
    private String cdnBaseUrl;

    /**
     * 保存图片文件并返回可访问的 URL
     *
     * @param userId 当前用户 ID（用于生成文件名）
     * @param file   上传的图片文件
     * @return 图片对外访问 URL
     */
    public String storeImage(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传图片文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        // 生成安全的文件名：feed-images/YYYY/MM/userId-uuid.ext
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!StringUtils.hasText(ext)) {
            ext = "jpg";
        }

        LocalDate today = LocalDate.now();
        String relativePath = String.format("feed-images/%d/%02d/%d-%s.%s",
                today.getYear(),
                today.getMonthValue(),
                userId,
                UUID.randomUUID().toString().replaceAll("-", ""),
                ext);

        if (useCdn && StringUtils.hasText(cdnBaseUrl)) {
            // 启用 CDN 时，仅生成 CDN 访问 URL
            String base = cdnBaseUrl.replaceAll("/+$", "");
            return base + "/" + relativePath;
        }

        // 本地存储：写入 feed.image.storage.root 目录
        Path root = Paths.get(imageStorageRoot);
        Path target = root.resolve(relativePath).normalize();

        // 确保目录存在
        Files.createDirectories(target.getParent());

        // 保存文件
        file.transferTo(target.toFile());

        // 对外暴露为 /feed-images/** 静态资源
        return "/feed-images/" + relativePath.substring("feed-images/".length());
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            return filename.substring(dot + 1);
        }
        return "";
    }
}

