package co.ke.proaktivio.qwanguapi.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rent")
public class RentPropertiesConfig {
    private String cronToCreateInvoice;
    private Integer dueDatePerMonth;
    private Integer penaltyPercentageOfRent;
}
