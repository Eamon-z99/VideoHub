package videobackend.video;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class VideoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoBackendApplication.class, args);
    }

}
