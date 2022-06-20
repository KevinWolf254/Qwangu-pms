package co.ke.proaktivio.qwanguapi.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "ftl")
public class FreeMarkerTemplatesPropertiesConfig {
    private List<Template> templates;

    @Data
    public static class Template {
        private String name;
        private String path;
        private List<String> models;
        private List<String> resources;
    }
}
