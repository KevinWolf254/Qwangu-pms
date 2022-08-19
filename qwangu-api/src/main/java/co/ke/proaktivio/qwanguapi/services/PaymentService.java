package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;

import java.util.Optional;

public interface PaymentService {
    Flux<Payment> findPaginated(Optional<String> id, Optional<Payment.Status> status,
                                Optional<Payment.Type> type, Optional<String> shortCode, Optional<String> referenceNo,
                                Optional<String> mobileNumber, int page, int pageSize, OrderType order);

}
