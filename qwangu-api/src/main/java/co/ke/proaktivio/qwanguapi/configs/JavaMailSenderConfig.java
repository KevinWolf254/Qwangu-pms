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

    private final EmailPropertiesConfig config;

    @Bean
    @Qualifier("customJavaMailSender")
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getMail().getHost());
        mailSender.setUsername(config.getMail().getUsername());
        mailSender.setPassword(config.getMail().getPassword());
        mailSender.setPort(config.getMail().getPort());
        mailSender.setDefaultEncoding("UTF-8");
        Properties pros = new Properties();
        pros.put("mail.smtp.auth", true);
        pros.put("mail.smtp.timeout", 25000);
        pros.put("mail.smtp.port", config.getMail().getPort());
        pros.put("mail.smtp.socketFactory.port", config.getMail().getPort());
        pros.put("mail.smtp.socketFactory.fallback", false);
        mailSender.setJavaMailProperties(pros);
        return mailSender;
    }
}
