package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.MpesaPayment;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MpesaPaymentService {

	Mono<MpesaPaymentResponse> validate(MpesaPaymentDto dto);

	Mono<MpesaPaymentResponse> create(MpesaPaymentDto dto);

	Mono<MpesaPayment> findById(String mpesaPaymentId);

	Mono<MpesaPayment> findByTransactionId(String transactionId);

	Flux<MpesaPayment> findAll(String transactionId, String referenceNumber, String shortCode, OrderType order);
}
