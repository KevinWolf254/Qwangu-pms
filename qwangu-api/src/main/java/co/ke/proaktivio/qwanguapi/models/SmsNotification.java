package co.ke.proaktivio.qwanguapi.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(value = "SMS_NOTIFICATION")
public class SmsNotification {
	
    @Id
    private String id;
    private NotificationStatus status;
    private String phoneNumber;
    private String message;
    @CreatedDate
    private LocalDateTime createdOn;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    
    public static class SmsNotificationBuilder {
        private String message;
        private String phoneNumber;

		public SmsNotificationBuilder setMessage(String message) {
			this.message = message;
			return this;
		}
		
		public SmsNotificationBuilder setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public SmsNotification build() {
			var notification = new SmsNotification();
			notification.setStatus(NotificationStatus.PENDING);
			notification.setPhoneNumber(phoneNumber);
			notification.setMessage(message);
			return notification;
		}

    }
}
