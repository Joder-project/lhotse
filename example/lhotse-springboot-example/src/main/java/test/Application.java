package test;

import org.lhotse.config.spring.EnableLhotse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableLhotse
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
