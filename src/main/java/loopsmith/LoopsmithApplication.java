package loopsmith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoopsmithApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoopsmithApplication.class, args);
    }
}
