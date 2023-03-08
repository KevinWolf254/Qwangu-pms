package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReceiptService {
	Mono<Receipt> create(ReceiptDto dto);

	Mono<Receipt> findById(String id);

	Flux<Receipt> findAll(String occupationId, String paymentId, OrderType order);

}
