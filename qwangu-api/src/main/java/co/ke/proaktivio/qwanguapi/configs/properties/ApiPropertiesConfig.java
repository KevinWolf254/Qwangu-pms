package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiPropertiesConfig {
    private List<String> endPoints;
}
