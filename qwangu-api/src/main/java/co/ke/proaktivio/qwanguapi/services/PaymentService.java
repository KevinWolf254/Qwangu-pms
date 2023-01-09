package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface PaymentService {
    Mono<Payment> findById(String paymentId);
    Flux<Payment> findAll(Optional<Payment.Status> status,
                          Optional<Payment.Type> type, Optional<String> shortCode, Optional<String> transactionId,
                          Optional<String> referenceNo, Optional<String> mobileNumber, OrderType order);

}
