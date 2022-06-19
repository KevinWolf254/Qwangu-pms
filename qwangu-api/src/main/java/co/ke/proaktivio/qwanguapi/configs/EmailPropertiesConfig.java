package co.ke.proaktivio.qwanguapi.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailPropertiesConfig {
    private EmailMailPropertiesConfig mail;
    private String noReply;
    private EmailTemplatePropertiesConfig template;
    private EmailResourcePropertiesConfig resource;

    @Data
    public static class EmailMailPropertiesConfig {
        private String host;
        private Integer port;
        private String username;
        private String password;
    }

    @Data
    public static class EmailTemplatePropertiesConfig {
        private String activateAccount;
    }

    @Data
    public static class EmailResourcePropertiesConfig {
        private String activateAccountUrl;
    }
}
