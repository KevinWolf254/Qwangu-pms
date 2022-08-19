package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "safaricom")
public class MpesaPropertiesConfig {
    private List<String> whiteListedUrls;
    private DarajaPropertiesConfig daraja;

    @Getter
    @Setter
    public static class DarajaPropertiesConfig {
        private List<String> urls;
        private BasicAuthenticationPropertiesConfig basicAuthentication;

        @Getter
        @Setter
        public static class BasicAuthenticationPropertiesConfig {
            private String consumerKey;
            private String consumerSecret;
        }
    }
}
