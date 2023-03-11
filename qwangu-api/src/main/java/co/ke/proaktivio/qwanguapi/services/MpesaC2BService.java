package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.MpesaC2BDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaC2BResponse;
import reactor.core.publisher.Mono;

public interface MpesaC2BService {
	@SuppressWarnings("rawtypes")
	Mono<MpesaC2BResponse> validate(MpesaC2BDto dto);

	@SuppressWarnings("rawtypes")
	Mono<MpesaC2BResponse> confirm(MpesaC2BDto dto);

	Mono<Payment> processPayment(Payment payment);
}
