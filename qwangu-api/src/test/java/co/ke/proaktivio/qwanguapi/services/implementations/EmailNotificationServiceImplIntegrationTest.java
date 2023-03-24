package co.ke.proaktivio.qwanguapi.services.implementations;

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
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.EmailNotificationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EmailNotificationServiceImplIntegrationTest {
	@Autowired
	private EmailNotificationService underTest;
	@Autowired
	private EmailNotificationRepository emailNotificationRepository;

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
	void update_returnsCustomNotFoundException_whenIdDoesNotExist() {
		// given
		var doNotExistId = "20";
		@SuppressWarnings("serial")
		List<String> toList = new ArrayList<>() {
			{
				add("person@somecompany.com");
			}
		};
		var emailNotification = new EmailNotification.EmailNotificationBuilder().subject("Testing").to(toList).build();
		// when
		Mono<EmailNotification> notFound = reset().then(underTest.update(doNotExistId, emailNotification));
		// then
		StepVerifier.create(notFound).expectErrorMatches(e -> e instanceof CustomNotFoundException
				&& e.getMessage().equals("EmailNotification with id %s does not exist!".formatted(doNotExistId)))
				.verify();
	}

	@Test
	void update_updatesEmailNotificationStatusToSent_whenEmailServiceReturnsTrue() {
		// given
		var emailNotificationId = "20";

		@SuppressWarnings("serial")
		List<String> toList = new ArrayList<>() {
			{
				add("person@somecompany.com");
			}
		};
		var emailNotification = new EmailNotification.EmailNotificationBuilder().subject("Testing").to(toList).build();
		emailNotification.setId(emailNotificationId);
		// when
		Mono<EmailNotification> updatesToTrue = reset().then(emailNotificationRepository.save(emailNotification))
				.doOnSuccess(en -> System.out.println("--- Created: " + en)).map(en -> {
					en.setStatus(NotificationStatus.SENT);
					return en;
				}).flatMap(en -> underTest.update(emailNotificationId, en));
		// then
		StepVerifier.create(updatesToTrue).expectNextMatches(en -> en.getStatus().equals(NotificationStatus.SENT))
				.verifyComplete();
	}

	@Test
	void findAll_returnsEmpty_whenEmailNotificationDoNotExist() {
		// when
		Flux<EmailNotification> findEmpty = reset().thenMany(underTest.findAll(null, null, null));
		// then
		StepVerifier.create(findEmpty).verifyComplete();
	}

	@Test
	void findAll_returnsEmailNotification_whenSuccessfullyFound() {
		// when
		update_updatesEmailNotificationStatusToSent_whenEmailServiceReturnsTrue();
		Flux<EmailNotification> findAll = underTest.findAll(null, null, null);
		// then
		StepVerifier.create(findAll).expectNextMatches(en -> en.getStatus().equals(NotificationStatus.SENT))
				.verifyComplete();

	}

}
