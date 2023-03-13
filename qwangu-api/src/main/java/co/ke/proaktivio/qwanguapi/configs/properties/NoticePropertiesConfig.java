package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notice")
public class NoticePropertiesConfig {
    private float levelOne;
    private float levelTwo;
    private float levelThree;
    private float levelFour;
    private float levelFive;
}
