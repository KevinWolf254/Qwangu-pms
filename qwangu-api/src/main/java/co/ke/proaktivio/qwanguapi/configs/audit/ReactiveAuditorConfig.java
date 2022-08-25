package co.ke.proaktivio.qwanguapi.configs.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@Configuration
@EnableReactiveMongoAuditing
public class ReactiveAuditorConfig {

    @Bean
    ReactiveAuditorAware<String> auditorAware() {
        return new ReactiveUsernameAuditor();
    }
}
