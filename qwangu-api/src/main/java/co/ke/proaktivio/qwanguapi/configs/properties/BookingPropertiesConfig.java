package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "booking")
public class BookingPropertiesConfig {
    private double percentage;
}
