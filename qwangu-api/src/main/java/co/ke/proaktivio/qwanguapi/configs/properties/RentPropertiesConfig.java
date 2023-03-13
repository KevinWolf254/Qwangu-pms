package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rent")
public class RentPropertiesConfig {
    private String cronToCreateInvoice;
    private Integer dueDatePerMonth;
    private Integer penaltyPercentageOfRent;
}
