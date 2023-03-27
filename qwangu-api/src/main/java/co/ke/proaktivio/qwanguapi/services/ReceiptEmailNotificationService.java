package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Payment;
import reactor.core.publisher.Mono;

public interface ReceiptEmailNotificationService {
	Mono<EmailNotification> create(Occupation occupation, Payment payment, OccupationTransaction previousOccupationTransaction);   	
}
