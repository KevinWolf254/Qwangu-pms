package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.EmailPropertiesConfig;
import co.ke.proaktivio.qwanguapi.pojos.Email;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender sender;
    private final FreeMarkerConfigurer freemarkerConfigurer;
    private final EmailPropertiesConfig config;

    public EmailServiceImpl(EmailPropertiesConfig config, @Qualifier("customFreeMarkerConfigurer") FreeMarkerConfigurer freemarkerConfigurer,
                            @Qualifier("customJavaMailSender") JavaMailSender sender) {
        this.config = config;
        this.freemarkerConfigurer = freemarkerConfigurer;
        this.sender = sender;
    }

    @Override
    public Mono<Boolean> send(Email email) {
        Mono<Boolean> blockingWrapper = Mono.fromCallable(() -> {
            boolean success = false;
            try {
                Template template = freemarkerConfigurer.getConfiguration().getTemplate(email.getTemplate());
                String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, email.getTemplateModel());

                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(config.getNoReply());
                helper.setTo(email.getTo().toArray(String[]::new));
                helper.setSubject(email.getSubject());
                helper.setText(htmlBody, true);
                if (email.getResources() != null && !email.getResources().isEmpty()) {
                    email
                            .getResources()
                            .forEach((name, resource) -> {
                                try {
                                    helper.addInline(name, resource);
                                } catch (MessagingException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
                sender.send(message);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        });
        blockingWrapper = blockingWrapper.subscribeOn(Schedulers.boundedElastic());
        return blockingWrapper;
    }
}
