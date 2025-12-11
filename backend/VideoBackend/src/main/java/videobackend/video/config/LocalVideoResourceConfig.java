package videobackend.video.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LocalVideoResourceConfig implements WebMvcConfigurer {

    @Value("${media.storage.root}")
    private String mediaStorageRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path basePath = Paths.get(mediaStorageRoot);
        String location = basePath.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/local-videos/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
