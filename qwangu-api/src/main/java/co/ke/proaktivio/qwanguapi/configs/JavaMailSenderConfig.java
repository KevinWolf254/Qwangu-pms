package co.ke.proaktivio.qwanguapi.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class JavaMailSenderConfig {
    private final MailPropertiesConfig config;

    @Bean
    @Qualifier("customJavaMailSender")
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        mailSender.setPort(config.getPort());
        mailSender.setDefaultEncoding("UTF-8");
        Properties pros = new Properties();
        pros.put("mail.smtp.auth", true);
        pros.put("mail.smtp.timeout", 25000);
        pros.put("mail.smtp.port", config.getPort());
        pros.put("mail.smtp.socketFactory.port", config.getPort());
        pros.put("mail.smtp.socketFactory.fallback", false);
        mailSender.setJavaMailProperties(pros);
        return mailSender;
    }
}
