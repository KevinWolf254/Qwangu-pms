package co.ke.proaktivio.qwanguapi.configs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notifications")
public class NotificationsPropertiesConfig {
    private boolean sendSms;
    private String senderId;
}
