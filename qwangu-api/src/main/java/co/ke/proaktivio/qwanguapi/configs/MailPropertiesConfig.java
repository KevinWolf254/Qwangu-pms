package co.ke.proaktivio.qwanguapi.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailPropertiesConfig {
    private String host;
    private Integer port;
    private String username;
    private String password;
}
