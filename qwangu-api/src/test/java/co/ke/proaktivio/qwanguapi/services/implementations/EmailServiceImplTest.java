package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.EmailPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.FreeMarkerConfig;
import co.ke.proaktivio.qwanguapi.configs.JavaMailSenderConfig;
import co.ke.proaktivio.qwanguapi.pojos.Email;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = EmailPropertiesConfig.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
        classes = {JavaMailSenderConfig.class, FreeMarkerConfig.class, EmailServiceImpl.class})
class EmailServiceImplTest {
    @Autowired
    @Qualifier("customJavaMailSender")
    private JavaMailSender sender;
    @Autowired
    @Qualifier("customFreeMarkerConfigurer")
    private FreeMarkerConfigurer freemarkerConfigurer;
    @Autowired
    private EmailPropertiesConfig config;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("person@gmail.com", "testing"))
            .withPerMethodLifecycle(false);

    @Autowired
    EmailServiceImpl emailService;

    @Test
    void init(){
        assertThat(this.config.getTemplate()).isNotNull();
        assertThat(this.config.getTemplate().getActivateAccount()).isEqualTo("activate_account.ftl");
        assertThat(this.config.getResource()).isNotNull();
        assertThat(this.config.getResource().getActivateAccountUrl()).isEqualTo("http://localhost:8080/user/activate");
        assertThat(this.config.getNoReply()).isEqualTo("noreply@proaktivio.co.ke");
    }

    @Test
    void send_returnsTrue_whenSuccessful() throws MessagingException {
        // given
        Email email = new Email();
        email.setTo(List.of("person@gmail.com"));
        email.setSubject("Account Activation");
        email.setTemplate(config.getTemplate().getActivateAccount());

        Map<String, Object> models = new HashMap<>();
        models.put("accountActivationUrl", config.getResource().getActivateAccountUrl());
        email.setTemplateModel(models);
        // when
        Mono<Boolean> send = emailService.send(email);
        // then
        StepVerifier
                .create(send)
                .expectNext(true)
                .verifyComplete();
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        assertThat(receivedMessage.getAllRecipients().length).isEqualTo(1);
        assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo("person@gmail.com");
    }
}