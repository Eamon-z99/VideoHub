package videobackend.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import videobackend.video.model.VideoItem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocalVideoService {

    private final JdbcTemplate jdbcTemplate;
    private final Path mediaRoot;

    public LocalVideoService(JdbcTemplate jdbcTemplate,
                             @Value("${media.storage.root}") String mediaStorageRoot) {
        this.jdbcTemplate = jdbcTemplate;
        this.mediaRoot = Paths.get(mediaStorageRoot);
    }

    public List<VideoItem> listPage(int page, int pageSize) {
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        String sql = """
                SELECT video_id, title, description, duration,
                       cover_url, storage_path, source_file,
                       view_count, file_size
                FROM videos
                ORDER BY create_time DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), safeSize, offset);
    }

    public long count() {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM videos", Long.class);
        return total == null ? 0 : total;
    }

    public Optional<VideoItem> findByVideoId(String videoId) {
        String sql = """
                SELECT video_id, title, description, duration,
                       cover_url, storage_path, source_file,
                       view_count, file_size
                FROM videos
                WHERE video_id = ?
                """;
        List<VideoItem> list = jdbcTemplate.query(sql, (rs, i) -> mapToVideo(rs), videoId);
        return list.stream().findFirst();
    }

    private VideoItem mapToVideo(ResultSet rs) throws SQLException {
        String sourceFile = rs.getString("source_file");
        String storagePath = rs.getString("storage_path");
        String coverFile = rs.getString("cover_url");
        return new VideoItem(
                rs.getString("video_id"),
                firstNonBlank(rs.getString("title"), "本地视频"),
                rs.getString("description"),
                rs.getObject("duration") == null ? null : rs.getInt("duration"),
                buildLocalUrl(sourceFile, coverFile),
                buildLocalUrl(sourceFile, storagePath),
                storagePath,
                sourceFile,
                rs.getObject("view_count") == null ? null : rs.getLong("view_count"),
                rs.getObject("file_size") == null ? null : rs.getLong("file_size")
        );
    }

    private String buildLocalUrl(String sourceFile, String fileName) {
        if (!StringUtils.hasText(sourceFile) || !StringUtils.hasText(fileName)) {
            return "";
        }

        // 兼容数据库中已存的 URL 编码路径或包含 % 的原始文件名
        String normalizedSource = normalizeSource(safeDecode(sourceFile));
        String normalizedFile = safeDecode(fileName).replace("\\", "/");

        // 判断 fileName 是否已经包含路径（包含斜杠）
        Path finalPath;
        if (normalizedFile.contains("/")) {
            // fileName 已经包含路径，直接使用
            finalPath = mediaRoot.resolve(normalizedFile).normalize();
        } else {
            // fileName 只是文件名，需要拼接 sourceFile 的目录部分
            Path sourcePath = Paths.get(normalizedSource);
            Path baseDir = sourcePath.getFileName() != null && hasFileExtension(sourcePath.getFileName().toString())
                    ? sourcePath.getParent()
                    : sourcePath;
            finalPath = mediaRoot.resolve(baseDir == null ? Paths.get("") : baseDir).resolve(normalizedFile).normalize();
        }

        // 安全检查：确保路径在 mediaRoot 内
        if (!finalPath.startsWith(mediaRoot)) {
            return "";
        }
        
        // 确定目录，同步使用目录实际文件名以避免编码差异
        Path dir = finalPath.getParent();
        boolean needImage = isImage(normalizedFile);

        // 先尝试按文件名在目录中直接匹配（处理 %, 全角/半角、规范化差异）
        Path foundByName = findFileByName(dir, normalizedFile, needImage);
        if (foundByName != null) {
            finalPath = foundByName;
        }

        // 优先尝试用数据库文件名直接匹配文件系统中的文件
        if (!java.nio.file.Files.exists(finalPath)) {
            // 在目录中按文件名精确搜索（处理文件名编码差异）
            Path found = findFileByName(dir, normalizedFile, needImage);
            if (found != null) {
                finalPath = found;
            } else {
                // 尝试其它常见扩展名（图片或视频）
                String baseName = stripExtension(normalizedFile);
                String[] imageExts = {".png", ".jpg", ".jpeg", ".PNG", ".JPG", ".JPEG"};
                String[] videoExts = {".mp4", ".MP4", ".mkv", ".MKV", ".mov", ".MOV"};
                String[] extList = needImage ? imageExts : videoExts;
                for (String ext : extList) {
                    Path altPath = dir.resolve(baseName + ext).normalize();
                    if (altPath.startsWith(mediaRoot) && java.nio.file.Files.exists(altPath)) {
                        finalPath = altPath;
                        break;
                    }
                }
            }
        }

        // 如果仍不存在，使用相似度匹配或取第一条同类型文件
        if (!java.nio.file.Files.exists(finalPath)) {
            Path byName = tryMatchByName(dir, stripExtension(normalizedFile), needImage);
            if (byName != null) {
                finalPath = byName;
            } else {
                Path first = pickFirstByType(dir, needImage);
                if (first != null) {
                    finalPath = first;
                } else {
                    return "";
                }
            }
        }

        // 构建相对路径用于URL编码
        Path relativePath = mediaRoot.relativize(finalPath);
        String encoded = Arrays.stream(relativePath.toString().replace("\\", "/").split("/"))
                .filter(StringUtils::hasText)
                .map(seg -> UriUtils.encodePathSegment(seg, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
        return "/local-videos/" + encoded;
    }

    /**
     * 尝试对字符串进行 URL 解码，失败则返回原值，避免包含 % 的普通文件名抛异常
     */
    private String safeDecode(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        try {
            return UriUtils.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignore) {
            return value;
        }
    }

    private String stripExtension(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return name;
    }

    private Path findFileByName(Path dir, String targetFileName, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String fileName = p.getFileName().toString();
                // 检查文件类型
                if (imageOnly && !isImage(fileName)) continue;
                if (!imageOnly && !isVideo(fileName)) continue;
                
                // 优先精确匹配（包括包含%的文件名）
                if (fileName.equals(targetFileName)) {
                    return p.normalize();
                }
                
                // 忽略大小写匹配
                if (fileName.equalsIgnoreCase(targetFileName)) {
                    return p.normalize();
                }
                
                // Unicode规范化匹配（但保留%字符）
                String normalizedTarget = normalizeFileNameWithPercent(targetFileName);
                String normalizedFile = normalizeFileNameWithPercent(fileName);
                if (normalizedTarget.equals(normalizedFile)) {
                    return p.normalize();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    
    /**
     * 规范化文件名用于匹配，但保留%字符（不删除）
     */
    private String normalizeFileNameWithPercent(String fileName) {
        // Unicode规范化，统一全角/半角、组合字符等
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        // 统一全角/半角标点（但保留%）
        normalized = normalized.replace('！', '!')
                .replace('？', '?')
                .replace('％', '%')  // 全角%转为半角%
                .replace('—', '-')
                .replace('–', '-')
                .replace('－', '-');
        // 注意：这里不删除%，保留%用于匹配
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeFileName(String fileName) {
        // Unicode规范化，统一全角/半角、组合字符等
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        // 统一全角/半角标点
        normalized = normalized.replace('！', '!')
                .replace('？', '?')
                .replace('％', '%')
                .replace('—', '-')
                .replace('–', '-')
                .replace('－', '-');
        // 移除 % 进行模糊匹配，避免 % 参与比对失败
        normalized = normalized.replace("%", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private boolean hasMultipleSpecialChars(String fileName) {
        // 检测文件名中是否包含多个特殊字符（如"？！"、"！"等全角标点）
        // 这些字符在URL编码后可能导致Spring资源处理器无法正确匹配文件
        int count = 0;
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            // 检测全角标点符号：！(U+FF01)、？(U+FF1F)、—(U+2014)等
            if (c == '！' || c == '？' || c == '—' || c == '–' || c == '－') {
                count++;
                // 如果出现2个或以上特殊字符，直接使用目录扫描
                if (count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private Path tryMatchByName(Path dir, String targetBase, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        String targetNorm = normalizeForCompare(targetBase).replace("%", "");
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String fileName = p.getFileName().toString();
                if (imageOnly && !isImage(fileName)) continue;
                if (!imageOnly && !isVideo(fileName)) continue;

                String candidateBase = stripExtension(fileName);
                String candidateNorm = normalizeForCompare(candidateBase).replace("%", "");
                if (candidateNorm.equals(targetNorm) || candidateNorm.contains(targetNorm) || targetNorm.contains(candidateNorm)) {
                    return p.normalize();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Path pickSingleByType(Path dir, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            Path candidate = null;
            int count = 0;
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (imageOnly && !isImage(name)) continue;
                if (!imageOnly && !isVideo(name)) continue;
                candidate = p.normalize();
                count++;
                if (count > 1) break;
            }
            return count == 1 ? candidate : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Path pickFirstByType(Path dir, boolean imageOnly) {
        if (dir == null || !java.nio.file.Files.isDirectory(dir)) {
            return null;
        }
        try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (imageOnly && !isImage(name)) continue;
                if (!imageOnly && !isVideo(name)) continue;
                return p.normalize();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isImage(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private boolean isVideo(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".mov");
    }

    private String normalizeForCompare(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFKC);
        normalized = normalized.replace('—', '-')
                .replace('–', '-')
                .replace('－', '-')
                .replace("%", "")
                .replace(" ", "")
                .replace("　", "");
        // 去除常见标点，仅保留字母数字和中日韩统一表意文字
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (Character.isLetterOrDigit(c) || isCjk(c) || c == '-') {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    private boolean isCjk(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS;
    }

    private boolean hasFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 && dot < name.length() - 1;
    }

    private String normalizeSource(String sourceFile) {
        String normalized = sourceFile.replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!StringUtils.hasText(normalized)) {
            return normalized;
        }
        Path rootFileName = mediaRoot.getFileName();
        if (rootFileName != null) {
            String rootName = rootFileName.toString().toLowerCase(Locale.ROOT);
            if (normalized.toLowerCase(Locale.ROOT).startsWith(rootName + "/")) {
                return normalized.substring(rootName.length() + 1);
            }
        }
        return normalized;
    }

    private String firstNonBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}




