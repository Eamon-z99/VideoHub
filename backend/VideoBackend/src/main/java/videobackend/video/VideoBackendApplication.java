package videobackend.video;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class VideoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoBackendApplication.class, args);
    }

}
