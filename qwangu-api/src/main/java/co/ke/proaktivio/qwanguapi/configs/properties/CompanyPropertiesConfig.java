package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "company")
public class CompanyPropertiesConfig {
    private String name;
    private String url;
    private String noReplyEmail;
    private SocialMediaPropertiesConfig socialMedia;

    @Getter
    @Setter
    public static class SocialMediaPropertiesConfig {
        private String linkedInUrl;
        private String twitterUrl;
        private String facebookUrl;
        private String instagramUrl;
    }
}
