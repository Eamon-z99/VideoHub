package videobackend.video.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注意：静态资源处理已改为使用 LocalVideoResourceController 来处理，
 * 以便正确处理包含 % 等特殊字符的文件名。
 * 此配置类保留为空，避免与Controller冲突。
 */
@Configuration
public class LocalVideoResourceConfig implements WebMvcConfigurer {
    // 资源处理已移至 LocalVideoResourceController
}



