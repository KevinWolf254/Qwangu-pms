package co.ke.proaktivio.qwanguapi.configs;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalErrorConfig {

    @Bean
    WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }
}
