package co.ke.proaktivio.qwanguapi.configs.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ftl")
public class FreeMarkerTemplatesPropertiesConfig {
    private List<Template> templates;

	@Getter
	@Setter
    public static class Template {
        private String name;
        private String path;
        private List<String> models;
        private List<String> resources;
    }
}
