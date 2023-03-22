package co.ke.proaktivio.qwanguapi.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;

public interface EmailNotificationRepository extends ReactiveMongoRepository<EmailNotification, String> {

}
