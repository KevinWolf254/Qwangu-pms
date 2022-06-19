package co.ke.proaktivio.qwanguapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    private List<String> to;
    private String subject;
    private String template;
    private Map<String, Object> templateModel;
    private Map<String, Resource> resources;
}
