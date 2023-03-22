package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender sender;
    private final FreeMarkerConfigurer freemarkerConfigurer;
    private final CompanyPropertiesConfig companyPropertiesConfig;

    public EmailServiceImpl(CompanyPropertiesConfig companyPropertiesConfig,
                            @Qualifier("customFreeMarkerConfigurer") FreeMarkerConfigurer freemarkerConfigurer,
                            @Qualifier("customJavaMailSender") JavaMailSender sender) {
        this.companyPropertiesConfig = companyPropertiesConfig;
        this.freemarkerConfigurer = freemarkerConfigurer;
        this.sender = sender;
    }

    @Override
    public Mono<Boolean> send(EmailNotification email) {
        Mono<Boolean> blockingWrapper = Mono.fromCallable(() -> {
                Template template = freemarkerConfigurer.getConfiguration().getTemplate(email.getTemplate());

                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(companyPropertiesConfig.getNoReplyEmail());
                helper.setTo(email.getTo().toArray(String[]::new));
                helper.setSubject(email.getSubject());
                if (email.getResources() != null && !email.getResources().isEmpty()) {
                    email
                            .getResources()
                            .forEach((name, resource) -> {
                                try {
                                    helper.addInline(name, new ClassPathResource(resource));
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }

                            });
                }
                String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, email.getTemplateModel());
                helper.setText(htmlBody, true);
                sender.send(message);
            return true;
        });
        blockingWrapper = blockingWrapper.subscribeOn(Schedulers.boundedElastic());
        return blockingWrapper;
    }
}
