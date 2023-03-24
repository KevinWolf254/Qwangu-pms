package co.ke.proaktivio.qwanguapi.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.EmailNotificationService;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Log4j2
@Component
@RequiredArgsConstructor
public class EmailNotificationJobManager {
	private final EmailNotificationService emailNotificationService;
	private final EmailService emailService;
	
	@Scheduled(cron = "0 0/5 * * * ?")
	void send() {
		sendEmailNotifications().subscribe();
	}

	public Flux<EmailNotification> sendEmailNotifications() {
		return emailNotificationService.findAll(NotificationStatus.PENDING, null, OrderType.ASC)
				.doOnNext(email -> log.info("Found: " +email))
				.flatMap(email -> {
					return emailService.send(email)
							.map(sent -> {
								email.setStatus(sent ? NotificationStatus.SENT : NotificationStatus.FAILED);
								return email;
							});
				})
				.flatMap(email -> emailNotificationService.update(email.getId(), email));
	}
}
