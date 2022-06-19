package co.ke.proaktivio.qwanguapi.configs;

import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;

import java.io.File;

@Configuration
public class FreeMarkerConfig {

    @Bean
    @Qualifier("freeMarkerConfiguration")
    public freemarker.template.Configuration configuration() {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_31);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        try {
            cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cfg;
    }

    @Bean
    @Qualifier("customFreeMarkerConfigurer")
    public FreeMarkerConfigurer customFreeMarkerConfigurer(@Qualifier("freeMarkerConfiguration") freemarker.template.Configuration configuration) {
        FreeMarkerConfigurer fmc = new FreeMarkerConfigurer();
        fmc.setConfiguration(configuration);
        return fmc;
    }

}
