package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import reactor.core.publisher.Mono;

public interface InvoiceEmailNotificationService {
	Mono<EmailNotification> create(Occupation occupation, Invoice invoice, OccupationTransaction previousOccupationTransaction);
}
