package videobackend.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MediaUrlResolver {
    private static final Pattern FEED_IMAGE_PATH_PATTERN = Pattern.compile("(?i)(https?://[^\\s)\\]\"']*?/feed-images/[^\\s)\\]\"']+|/feed-images/[^\\s)\\]\"']+)");

    private final boolean avatarCdnEnabled;
    private final String avatarCdnBaseUrl;
    private final boolean feedImageCdnEnabled;
    private final String feedImageCdnBaseUrl;

    public MediaUrlResolver(
            @Value("${avatar.cdn.enabled:${media.cdn.enabled:false}}") boolean avatarCdnEnabled,
            @Value("${avatar.cdn.base-url:${media.cdn.base-url:}}") String avatarCdnBaseUrl,
            @Value("${feed.image.cdn.enabled:${media.cdn.enabled:false}}") boolean feedImageCdnEnabled,
            @Value("${feed.image.cdn.base-url:${media.cdn.base-url:}}") String feedImageCdnBaseUrl) {
        this.avatarCdnEnabled = avatarCdnEnabled;
        this.avatarCdnBaseUrl = trimRightSlash(avatarCdnBaseUrl);
        this.feedImageCdnEnabled = feedImageCdnEnabled;
        this.feedImageCdnBaseUrl = trimRightSlash(feedImageCdnBaseUrl);

        if (this.avatarCdnEnabled && !StringUtils.hasText(this.avatarCdnBaseUrl)) {
            throw new IllegalStateException("avatar.cdn.enabled=true 但未配置 avatar.cdn.base-url");
        }
        if (this.feedImageCdnEnabled && !StringUtils.hasText(this.feedImageCdnBaseUrl)) {
            throw new IllegalStateException("feed.image.cdn.enabled=true 但未配置 feed.image.cdn.base-url");
        }
    }

    public String resolveAvatar(String raw) {
        return resolveAvatarInternal(raw);
    }

    public String resolveFeedImage(String raw) {
        return resolve(raw, feedImageCdnEnabled, feedImageCdnBaseUrl, "/feed-images/");
    }

    /**
     * 解析富文本/消息正文中内嵌的 feed-images 链接（如 Markdown 图片、[IMAGE:...]）。
     */
    public String resolveFeedImageInText(String rawText) {
        if (!StringUtils.hasText(rawText) || !feedImageCdnEnabled) {
            return rawText;
        }
        Matcher matcher = FEED_IMAGE_PATH_PATTERN.matcher(rawText);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String original = matcher.group(1);
            String resolved = resolveFeedImage(original);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolve(String raw, boolean enabled, String baseUrl, String markerPath) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String value = raw.trim();
        if (!enabled) {
            // CDN 关闭时：若历史值是绝对 URL（CDN/旧域名），尽量回退到本地相对路径（/avatars/**、/feed-images/**）
            if (value.startsWith("http://") || value.startsWith("https://")) {
                String localPath = extractMarkerPath(value, markerPath);
                if (StringUtils.hasText(localPath)) {
                    return localPath;
                }
            }
            return value;
        }

        // 先处理已是绝对 URL 的历史值：如果是本地/旧域名路径，抽出 markerPath 后改写到 CDN
        if (value.startsWith("http://") || value.startsWith("https://")) {
            String rewritten = rewriteAbsoluteUrlToCdn(value, baseUrl, markerPath);
            return rewritten != null ? rewritten : value;
        }

        if (value.startsWith("/")) {
            return baseUrl + value;
        }
        return baseUrl + "/" + value;
    }

    /**
     * 头像 URL 解析规则（兼容历史存量）：
     * - 数据库 users.avatar 推荐存储“相对 key”（如 2026/03/xxx.jpg），由后端按开关拼接完整 URL
     * - 兼容历史值可能包含：/avatars/**、avatars/**、CDN 绝对 URL、以及错误的 .../Avatars/avatars/**（重复目录）
     *
     * 目标：当 avatar.cdn.enabled=true 时，对外统一输出：
     *   {avatar.cdn.base-url}/{key}
     * 其中 key 不包含 avatars/ 或 /avatars/ 前缀。
     */
    private String resolveAvatarInternal(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String value = raw.trim();

        // 1) CDN 关闭：尽量回退到本地 /avatars/** 访问路径
        if (!avatarCdnEnabled) {
            if (value.startsWith("http://") || value.startsWith("https://")) {
                // 尝试从绝对 URL 中抽取头像 key（兼容 /avatars/ 与 /Avatars/）
                String key = extractAvatarKeyFromAbsoluteUrl(value);
                return StringUtils.hasText(key) ? ("/avatars/" + key) : value;
            }
            if (value.startsWith("/avatars/")) {
                return value;
            }
            if (value.startsWith("avatars/")) {
                return "/avatars/" + value.substring("avatars/".length());
            }
            if (value.startsWith("/")) {
                // 非标准但保持原样（可能是 /2026/...）
                return value;
            }
            // 兜底：视为 key
            return "/avatars/" + value;
        }

        // 2) CDN 开启：统一输出 {baseUrl}/{key}
        if (value.startsWith("http://") || value.startsWith("https://")) {
            // 若已经是以 baseUrl 开头的 URL，仍需修正可能的重复 /avatars/ 前缀
            if (startsWithIgnoreCase(value, avatarCdnBaseUrl)) {
                String rest = value.substring(avatarCdnBaseUrl.length());
                String key = normalizeAvatarKey(rest);
                return avatarCdnBaseUrl + "/" + key;
            }

            // 其他域名或历史绝对 URL：尽量抽取 key 后映射到当前 CDN baseUrl
            String key = extractAvatarKeyFromAbsoluteUrl(value);
            if (StringUtils.hasText(key)) {
                return avatarCdnBaseUrl + "/" + key;
            }
            return value;
        }

        // 非绝对 URL：允许 /avatars/**、avatars/**、/key、key
        String key = normalizeAvatarKey(value);
        return avatarCdnBaseUrl + "/" + key;
    }

    /**
     * 头像在 CDN 上的相对路径（key）规范化：
     * - 不包含 query/hash
     * - 不以 "/" 开头
     * - **不剥离** "avatars/" 前缀（因为 COS 存量对象路径实际为 Avatars/avatars/**）
     *
     * 这与“完整地址拼接逻辑 = {baseUrl} + users.avatar 字段”保持一致：
     * - users.avatar 可能是 "/avatars/xxx" 或 "avatars/xxx" 或 "xxx"
     */
    private String normalizeAvatarKey(String maybeKey) {
        if (!StringUtils.hasText(maybeKey)) {
            return "";
        }
        String k = maybeKey.trim();

        // 去掉 query/hash
        int q = k.indexOf('?');
        if (q >= 0) k = k.substring(0, q);
        int h = k.indexOf('#');
        if (h >= 0) k = k.substring(0, h);

        // 去掉开头的 base path 分隔符
        while (k.startsWith("/")) {
            k = k.substring(1);
        }
        return k;
    }

    private String extractAvatarKeyFromAbsoluteUrl(String absoluteUrl) {
        try {
            URI uri = new URI(absoluteUrl);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }

            // 优先：从 "/avatars/"（大小写不敏感）后抽 key
            int idx = indexOfIgnoreCase(path, "/avatars/");
            if (idx >= 0) {
                String key = path.substring(idx + "/avatars/".length());
                return normalizeAvatarKey(key);
            }

            // 其次：如果绝对 URL 本身就在 baseUrl 下（但可能出现 .../Avatars/avatars/...），抽取 baseUrl 之后的部分
            if (StringUtils.hasText(avatarCdnBaseUrl) && startsWithIgnoreCase(absoluteUrl, avatarCdnBaseUrl)) {
                String rest = absoluteUrl.substring(avatarCdnBaseUrl.length());
                return normalizeAvatarKey(rest);
            }

            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private boolean startsWithIgnoreCase(String s, String prefix) {
        if (s == null || prefix == null) return false;
        if (s.length() < prefix.length()) return false;
        return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private int indexOfIgnoreCase(String s, String needle) {
        if (s == null || needle == null) return -1;
        final int n = s.length();
        final int m = needle.length();
        if (m == 0) return 0;
        for (int i = 0; i <= n - m; i++) {
            if (s.regionMatches(true, i, needle, 0, m)) {
                return i;
            }
        }
        return -1;
    }

    private String extractMarkerPath(String absoluteUrl, String markerPath) {
        try {
            URI uri = new URI(absoluteUrl);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }
            int idx = path.indexOf(markerPath);
            if (idx < 0) {
                return null;
            }
            return path.substring(idx);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private String rewriteAbsoluteUrlToCdn(String absoluteUrl, String baseUrl, String markerPath) {
        try {
            URI uri = new URI(absoluteUrl);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }
            int idx = path.indexOf(markerPath);
            if (idx < 0) {
                return null;
            }
            String suffix = path.substring(idx);
            return baseUrl + suffix;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private String trimRightSlash(String s) {
        if (!StringUtils.hasText(s)) {
            return "";
        }
        return s.trim().replaceAll("/+$", "");
    }
}

