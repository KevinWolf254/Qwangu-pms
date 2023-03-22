package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.SmsNotification;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SmsNotificationRepository extends ReactiveMongoRepository<SmsNotification, String> {
}
