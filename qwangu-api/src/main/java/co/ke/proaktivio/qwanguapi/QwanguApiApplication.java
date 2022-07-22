package co.ke.proaktivio.qwanguapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan("co.ke.proaktivio.qwanguapi.*")
public class QwanguApiApplication {

    public static void main(String[] args) {
		SpringApplication.run(QwanguApiApplication.class, args);
    }

}
