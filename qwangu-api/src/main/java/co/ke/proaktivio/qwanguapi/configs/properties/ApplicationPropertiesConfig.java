package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationPropertiesConfig {
    private List<String> endPoints;
}