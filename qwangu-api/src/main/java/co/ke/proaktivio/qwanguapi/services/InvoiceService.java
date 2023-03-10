package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvoiceService {
	Mono<Invoice> create(InvoiceDto dto);

	Mono<Invoice> findById(String invoiceId);

	Flux<Invoice> findAll(Invoice.Type type, String invoiceNo, String occupationId, OrderType order);

	Mono<Boolean> deleteById(String id);
}
