package co.ke.proaktivio.qwanguapi.pojos;

import java.util.Map;

import org.springframework.core.io.Resource;

public interface IEmailNotification {
	public String getSubject();
	public String getTemplate();
	public Map<String, Object> getTemplateModel();
	public Map<String, Resource> getResources();
}
