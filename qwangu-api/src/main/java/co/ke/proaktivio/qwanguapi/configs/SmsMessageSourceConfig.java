package co.ke.proaktivio.qwanguapi.configs;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class SmsMessageSourceConfig {

    @Bean
    public MessageSource smsMessageSource() {
    	ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:sms_messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
