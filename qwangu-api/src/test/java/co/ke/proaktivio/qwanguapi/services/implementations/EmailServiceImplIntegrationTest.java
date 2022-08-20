package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.properties.ApplicationPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.pojos.Email;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmailServiceImplIntegrationTest {
    @Autowired
    @Qualifier("customJavaMailSender")
    private JavaMailSender sender;
    @Autowired
    @Qualifier("customFreeMarkerConfigurer")
    private FreeMarkerConfigurer fc;
    @Autowired
    private CompanyPropertiesConfig cpc;
    @Autowired
    private FreeMarkerTemplatesPropertiesConfig fmpc;
    @Autowired
    private ApplicationPropertiesConfig apc;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("person@gmail.com", "testing"))
            .withPerMethodLifecycle(false);

    @Autowired
    EmailServiceImpl emailService;

    @Test
    void init(){
        assertThat(this.fmpc.getTemplates().get(1)).isNotNull();
        assertThat(this.fmpc.getTemplates().get(1).getName()).isEqualTo("activate_account.ftlh");
        assertThat(this.fmpc.getTemplates().get(1).getPath()).isEqualTo("src/main/resources/templates");
        assertThat(this.fmpc.getTemplates().get(0).getModels().get(0)).isEqualTo("https://linked-in.com");
        assertThat(this.fmpc.getTemplates().get(0).getModels().get(1)).isEqualTo("https://twitter.com");
        assertThat(this.fmpc.getTemplates().get(0).getModels().get(2)).isEqualTo("https://facebook.com");
        assertThat(this.fmpc.getTemplates().get(0).getModels().get(3)).isEqualTo("https://instagram.com");

        assertThat(this.cpc.getName()).isEqualTo("proaktivio");
        assertThat(this.cpc.getUrl()).isEqualTo("https://proaktivio.co.ke");
        assertThat(this.cpc.getNoReplyEmail()).isEqualTo("noreply@proaktivio.co.ke");
        assertThat(this.cpc.getSocialMedia().getLinkedInUrl()).isEqualTo("https://linked-in.com");
        assertThat(this.cpc.getSocialMedia().getTwitterUrl()).isEqualTo("https://twitter.com");
        assertThat(this.cpc.getSocialMedia().getFacebookUrl()).isEqualTo("https://facebook.com");
        assertThat(this.cpc.getSocialMedia().getInstagramUrl()).isEqualTo("https://instagram.com");
    }

    @Test
    void send_returnsTrue_whenSuccessful() throws MessagingException {
        // given
        Email email = new Email();
        email.setTo(List.of("person@gmail.com"));
        email.setSubject("Account Activation");
        email.setTemplate(this.fmpc.getTemplates().get(1).getName());

        Map<String, Object> models = new HashMap<>();
        models.put("companyUrl", cpc.getUrl());
        models.put("accountActivationUrl", apc.getEndPoints().get(0));
        models.put("linkedInUrl", fmpc.getTemplates().get(0).getModels().get(0));
        models.put("twitterUrl", fmpc.getTemplates().get(0).getModels().get(1));
        models.put("facebookUrl", fmpc.getTemplates().get(0).getModels().get(2));
        models.put("instagramUrl", fmpc.getTemplates().get(0).getModels().get(3));
        models.put("designedBy", fmpc.getTemplates().get(0).getModels().get(4));
        email.setTemplateModel(models);

        Map<String, Resource> resourceMap = new HashMap<>();
        List<String> resources = fmpc.getTemplates().get(0).getResources();
        resourceMap.put("companyLogoImage", new ClassPathResource(resources.get(0)));
        resourceMap.put("passwordImage", new ClassPathResource(resources.get(1)));
        resourceMap.put("footerImage", new ClassPathResource(resources.get(2)));
        resourceMap.put("linkedInImage", new ClassPathResource(resources.get(3)));
        resourceMap.put("twitterImage", new ClassPathResource(resources.get(4)));
        resourceMap.put("facebookImage", new ClassPathResource(resources.get(5)));
        resourceMap.put("instagramImage", new ClassPathResource(resources.get(6)));
        email.setResources(resourceMap);

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


    @Test
    void send_returnsMailSendException_whenResourceIsNotFound() throws MessagingException {

        // given
        Email email = new Email();
        email.setTo(List.of("person@gmail.com"));
        email.setSubject("Account Activation");
        email.setTemplate(this.fmpc.getTemplates().get(1).getName());

        Map<String, Object> models = new HashMap<>();
        models.put("companyUrl", cpc.getUrl());
        models.put("accountActivationUrl", apc.getEndPoints().get(0));
        models.put("linkedInUrl", fmpc.getTemplates().get(0).getModels().get(0));
        models.put("twitterUrl", fmpc.getTemplates().get(0).getModels().get(1));
        models.put("facebookUrl", fmpc.getTemplates().get(0).getModels().get(2));
        models.put("instagramUrl", fmpc.getTemplates().get(0).getModels().get(3));
        models.put("designedBy", fmpc.getTemplates().get(0).getModels().get(4));
        email.setTemplateModel(models);

        Map<String, Resource> resourceMap = new HashMap<>();
        List<String> resources = fmpc.getTemplates().get(0).getResources();
        resourceMap.put("companyLogoImage", new ClassPathResource(resources.get(0)));
        resourceMap.put("passwordImage", new ClassPathResource(resources.get(1)));
        resourceMap.put("footerImage", new ClassPathResource(resources.get(2)));
        resourceMap.put("linkedInImage", new ClassPathResource(resources.get(3)));
        resourceMap.put("twitterImage", new ClassPathResource(resources.get(4)));
        resourceMap.put("facebookImage", new ClassPathResource(resources.get(5)));
        resourceMap.put("instagramImage", new ClassPathResource(resources.get(6)));
        email.setResources(resourceMap);
        // when
        // then
        StepVerifier
                .create(emailService.send(email))
                .expectNext(true)
                .verifyComplete();
    }
}