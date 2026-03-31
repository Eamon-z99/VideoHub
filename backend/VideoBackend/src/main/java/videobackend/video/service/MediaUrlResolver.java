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
        return resolve(raw, avatarCdnEnabled, avatarCdnBaseUrl, "/avatars/");
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

