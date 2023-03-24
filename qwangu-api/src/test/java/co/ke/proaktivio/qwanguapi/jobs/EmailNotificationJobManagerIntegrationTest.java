package co.ke.proaktivio.qwanguapi.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EmailNotificationJobManagerIntegrationTest {
	@Autowired
	private EmailNotificationJobManager underTest;
	@Autowired
	private EmailNotificationRepository emailNotificationRepository;
	@MockBean
	private EmailService emailService;
	@MockBean
	private BootstrapConfig bootstrapConfig;
	@MockBean
	private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(
			DockerImageName.parse("mongo:latest"));

	@DynamicPropertySource
	public static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	private Mono<Void> reset() {
		return emailNotificationRepository.deleteAll()
				.doOnSuccess(t -> System.out.println("---- Deleted all EmailNotifications!"));
	}

	@Test
	void sendEmailNotifications_returnsEmpty_whenEmailWithStatusPendingDoNotExist() {
		// given
		// when
		Flux<EmailNotification> enEmpty = reset().thenMany(underTest.sendEmailNotifications());
		// then
		StepVerifier.create(enEmpty).verifyComplete();
	}

	@Test
	void sendEmailNotifications_updatesEmailNotificationStatusToFailed_whenEmailServiceReturnsFalse() {
		// given
		@SuppressWarnings("serial")
		List<String> toList = new ArrayList<>() {
			{
				add("person@somecompany.com");
			}
		};
		var emailNotification = new EmailNotification.EmailNotificationBuilder().subject("Testing").to(toList).build();
		// when
		when(emailService.send(any(EmailNotification.class))).thenReturn(Mono.just(false));
		Flux<EmailNotification> sendReturnsFalse = reset().then(emailNotificationRepository.save(emailNotification))
				.thenMany(underTest.sendEmailNotifications()).doOnNext(en -> System.out.println("--- Updated: " + en));
		// then
		StepVerifier.create(sendReturnsFalse).expectNextMatches(en -> en.getStatus().equals(NotificationStatus.FAILED))
				.verifyComplete();
	}

	@Test
	void sendEmailNotifications_updatesEmailNotificationStatusToSent_whenEmailServiceReturnsTrue() {
		// given
		@SuppressWarnings("serial")
		List<String> toList = new ArrayList<>() {
			{
				add("person@somecompany.com");
			}
		};
		var emailNotification = new EmailNotification.EmailNotificationBuilder().subject("Testing").to(toList).build();
		// when
		when(emailService.send(any(EmailNotification.class))).thenReturn(Mono.just(true));
		Flux<EmailNotification> sendReturnsFalse = reset().then(emailNotificationRepository.save(emailNotification))
				.thenMany(underTest.sendEmailNotifications()).doOnNext(en -> System.out.println("--- Updated: " + en));
		// then
		StepVerifier.create(sendReturnsFalse).expectNextMatches(en -> en.getStatus().equals(NotificationStatus.SENT))
				.verifyComplete();
	}

	@Test
	void sendEmailNotifications_returnsEmpty_whenOnNextAfterEmailServiceReturnsTrue() {
		// given
		// when
		sendEmailNotifications_updatesEmailNotificationStatusToSent_whenEmailServiceReturnsTrue();
		Flux<EmailNotification> enEmpty = underTest.sendEmailNotifications();
		// then
		StepVerifier.create(enEmpty).verifyComplete();
	}
}
