package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.*;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Email;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = {MailPropertiesConfig.class, CompanyPropertiesConfig.class,
        ApplicationPropertiesConfig.class, FreeMarkerTemplatesPropertiesConfig.class})
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
        classes = {EmailGeneratorImpl.class})
class EmailGeneratorImplTest {
    @Autowired
    private CompanyPropertiesConfig cpc;
    @Autowired
    private FreeMarkerTemplatesPropertiesConfig fmpc;
    @Autowired
    private ApplicationPropertiesConfig apc;
    @Autowired
    private EmailGeneratorImpl emailGenerator;

    @Test
    void generateAccountActivationEmail() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null);
        var token = UUID.randomUUID().toString();
        // when
        Email email = emailGenerator.generateAccountActivationEmail(user, token);
        // then
        assertThat(email).isNotNull();
        assertThat(email.getTo().get(0)).isEqualTo(emailAddress).isNotEmpty();
        assertThat(email.getSubject()).isEqualTo("Account Activation");
        assertThat(email.getTemplate()).isEqualTo(fmpc.getTemplates().get(1).getName());

        Map<String, Object> templateModel = email.getTemplateModel();
        assertThat(templateModel).isNotEmpty();
        assertThat(templateModel.get("companyUrl")).isEqualTo(cpc.getUrl());
        var activationUrl = apc.getEndPoints().get(0) + apc.getEndPoints().get(1) + user.getId() + "/activate" + "/?token=" + token;
        assertThat(templateModel.get("accountActivationUrl")).isEqualTo(activationUrl);
        assertThat(templateModel.get("linkedInUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(0));
        assertThat(templateModel.get("twitterUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(1));
        assertThat(templateModel.get("facebookUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(2));
        assertThat(templateModel.get("instagramUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(3));
        assertThat(templateModel.get("designedBy")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(4));

        Map<String, Resource> resources = email.getResources();
        assertThat(resources).isNotEmpty();
        List<String> resourcesDir = fmpc.getTemplates().get(0).getResources();
        assertThat(resources.get("companyLogoImage")).isEqualTo(new ClassPathResource(resourcesDir.get(0)));
        assertThat(resources.get("passwordImage")).isEqualTo(new ClassPathResource(resourcesDir.get(1)));
        assertThat(resources.get("footerImage")).isEqualTo(new ClassPathResource(resourcesDir.get(2)));
        assertThat(resources.get("linkedInImage")).isEqualTo(new ClassPathResource(resourcesDir.get(3)));
        assertThat(resources.get("twitterImage")).isEqualTo(new ClassPathResource(resourcesDir.get(4)));
        assertThat(resources.get("facebookImage")).isEqualTo(new ClassPathResource(resourcesDir.get(5)));
        assertThat(resources.get("instagramImage")).isEqualTo(new ClassPathResource(resourcesDir.get(6)));

    }

    @Test
    void generatePasswordForgottenEmail() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null);
        var token = UUID.randomUUID().toString();
        // when
        Email email = emailGenerator.generatePasswordForgottenEmail(user, token);
        // then
        assertThat(email).isNotNull();
        assertThat(email.getTo().get(0)).isEqualTo(emailAddress).isNotEmpty();
        assertThat(email.getSubject()).isEqualTo("Password Reset");
        assertThat(email.getTemplate()).isEqualTo(fmpc.getTemplates().get(2).getName());

        Map<String, Object> templateModel = email.getTemplateModel();
        assertThat(templateModel).isNotEmpty();
        assertThat(templateModel.get("companyUrl")).isEqualTo(cpc.getUrl());
        var resetPasswordUrl = apc.getEndPoints().get(0) + apc.getEndPoints().get(1) + "/token" + "/?token=" + token;
        assertThat(templateModel.get("resetPasswordUrl")).isEqualTo(resetPasswordUrl);
        assertThat(templateModel.get("linkedInUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(0));
        assertThat(templateModel.get("twitterUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(1));
        assertThat(templateModel.get("facebookUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(2));
        assertThat(templateModel.get("instagramUrl")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(3));
        assertThat(templateModel.get("designedBy")).isEqualTo(fmpc.getTemplates().get(0).getModels().get(4));

        Map<String, Resource> resources = email.getResources();
        assertThat(resources).isNotEmpty();
        List<String> resourcesDir = fmpc.getTemplates().get(0).getResources();
        List<String> resourcesDir2 = fmpc.getTemplates().get(2).getResources();
        assertThat(resources.get("companyLogoImage")).isEqualTo(new ClassPathResource(resourcesDir.get(0)));
        assertThat(resources.get("passwordImage")).isEqualTo(new ClassPathResource(resourcesDir2.get(0)));
        assertThat(resources.get("footerImage")).isEqualTo(new ClassPathResource(resourcesDir.get(2)));
        assertThat(resources.get("linkedInImage")).isEqualTo(new ClassPathResource(resourcesDir.get(3)));
        assertThat(resources.get("twitterImage")).isEqualTo(new ClassPathResource(resourcesDir.get(4)));
        assertThat(resources.get("facebookImage")).isEqualTo(new ClassPathResource(resourcesDir.get(5)));
        assertThat(resources.get("instagramImage")).isEqualTo(new ClassPathResource(resourcesDir.get(6)));

    }
}