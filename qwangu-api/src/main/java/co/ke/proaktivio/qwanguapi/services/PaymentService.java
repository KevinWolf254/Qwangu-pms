package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentService {
	Mono<Payment> create(Payment payment);

	Mono<Payment> update(Payment payment);

	Mono<Payment> findById(String paymentId);

	Flux<Payment> findAll(PaymentStatus status, PaymentType type, String referenceNumber, OrderType order);

}
