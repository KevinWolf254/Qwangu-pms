package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import reactor.core.publisher.Mono;

public interface DarajaCustomerToBusinessService {
	@SuppressWarnings("rawtypes")
	Mono<DarajaCustomerToBusinessResponse> validate(DarajaCustomerToBusinessDto dto);

	@SuppressWarnings("rawtypes")
	Mono<DarajaCustomerToBusinessResponse> confirm(DarajaCustomerToBusinessDto dto);

	Mono<Payment> processPayment(Payment payment);
}
