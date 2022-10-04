package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ReceiptService {
    Mono<Receipt> create(ReceiptDto dto);
    Mono<Receipt> findById(String id);
    Flux<Receipt> findPaginated(Optional<String> occupationId, Optional<String> paymentId, int page, int pageSize,
                                   OrderType order);

}
