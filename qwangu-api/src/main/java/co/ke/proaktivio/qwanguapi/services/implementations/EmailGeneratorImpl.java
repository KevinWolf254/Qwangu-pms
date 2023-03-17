package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.properties.ApplicationPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Email;
import co.ke.proaktivio.qwanguapi.services.EmailGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailGeneratorImpl implements EmailGenerator {
    private final FreeMarkerTemplatesPropertiesConfig fmpc;
    private final CompanyPropertiesConfig cpc;
    private final ApplicationPropertiesConfig apc;

    @Override
    public Email generateAccountActivationEmail(User user, String token) {
        Email email = new Email();
        email.setTo(List.of(user.getEmailAddress()));
        email.setSubject("Account Activation");
        email.setTemplate(fmpc.getTemplates().get(1).getName());

        Map<String, Object> models = new HashMap<>();
        models.put("companyUrl", cpc.getUrl());
        var activationUrl = apc.getEndPoints().get(0) + apc.getEndPoints().get(1) + "activate/" + "?token=" + token;
        models.put("accountActivationUrl", activationUrl);
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
        return email;
    }


    @Override
    public Email generatePasswordForgottenEmail(User user, String token) {
        Email email = new Email();
        email.setTo(List.of(user.getEmailAddress()));
        email.setSubject("Password Reset");
        email.setTemplate(fmpc.getTemplates().get(2).getName());

        Map<String, Object> models = new HashMap<>();
        models.put("companyUrl", cpc.getUrl());
        var resetPasswordUrl = apc.getEndPoints().get(0) + apc.getEndPoints().get(1) + "/token" + "/?token=" + token;
        models.put("resetPasswordUrl", resetPasswordUrl);
        models.put("linkedInUrl", fmpc.getTemplates().get(0).getModels().get(0));
        models.put("twitterUrl", fmpc.getTemplates().get(0).getModels().get(1));
        models.put("facebookUrl", fmpc.getTemplates().get(0).getModels().get(2));
        models.put("instagramUrl", fmpc.getTemplates().get(0).getModels().get(3));
        models.put("designedBy", fmpc.getTemplates().get(0).getModels().get(4));
        email.setTemplateModel(models);

        Map<String, Resource> resourceMap = new HashMap<>();
        List<String> resources = fmpc.getTemplates().get(0).getResources();
        List<String> resources2 = fmpc.getTemplates().get(2).getResources();
        resourceMap.put("companyLogoImage", new ClassPathResource(resources.get(0)));
        resourceMap.put("passwordImage", new ClassPathResource(resources2.get(0)));
        resourceMap.put("footerImage", new ClassPathResource(resources.get(2)));
        resourceMap.put("linkedInImage", new ClassPathResource(resources.get(3)));
        resourceMap.put("twitterImage", new ClassPathResource(resources.get(4)));
        resourceMap.put("facebookImage", new ClassPathResource(resources.get(5)));
        resourceMap.put("instagramImage", new ClassPathResource(resources.get(6)));
        email.setResources(resourceMap);
        return email;
    }
}
