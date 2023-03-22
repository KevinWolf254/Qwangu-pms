package co.ke.proaktivio.qwanguapi.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(value = "EMAIL_NOTIFICATION")
public class EmailNotification {
    @Id
    private String id;
    private NotificationStatus status;
    private List<String> to;
    private String subject;
    private String template;
    private Map<String, Object> templateModel;
    private Map<String, String> resources;
    @CreatedDate
    private LocalDateTime createdOn;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    
    public static class EmailNotificationBuilder {
        private List<String> to;
        private String subject;
        private String template;
        private Map<String, Object> templateModel;
        private Map<String, String> resources;

		public EmailNotificationBuilder to(List<String> to) {
			this.to = to;
			return this;
		}
		public EmailNotificationBuilder subject(String subject) {
			this.subject = subject;
			return this;
		}
		public EmailNotificationBuilder template(String template) {
			this.template = template;
			return this;
		}
		public EmailNotificationBuilder templateModel(Map<String, Object> templateModel) {
			this.templateModel = templateModel;
			return this;
		}
		public EmailNotificationBuilder resources(Map<String, String> resources) {
			this.resources = resources;
			return this;
		}

		public EmailNotification build() {
			var notification = new EmailNotification();
			notification.setStatus(NotificationStatus.PENDING);
			notification.setTo(to);
			notification.setSubject(subject);
			notification.setTemplate(template);
			notification.setTemplateModel(templateModel);
			notification.setResources(resources);
			return notification;
		}

    }
}
