package co.ke.proaktivio.qwanguapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@ComponentScan("co.ke.proaktivio.qwanguapi.security")
@ComponentScan("co.ke.proaktivio.qwanguapi.configs")
@SpringBootApplication
public class QwanguApiApplication {

    public static void main(String[] args) {
		SpringApplication.run(QwanguApiApplication.class, args);
    }

}
