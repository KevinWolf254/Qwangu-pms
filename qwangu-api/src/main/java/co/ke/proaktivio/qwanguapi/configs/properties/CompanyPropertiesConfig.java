package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "company")
public class CompanyPropertiesConfig {
    private String name;
    private String url;
    private String noReplyEmail;
    private SocialMediaPropertiesConfig socialMedia;

    @Data
    public static class SocialMediaPropertiesConfig {
        private String linkedInUrl;
        private String twitterUrl;
        private String facebookUrl;
        private String instagramUrl;
    }
}
