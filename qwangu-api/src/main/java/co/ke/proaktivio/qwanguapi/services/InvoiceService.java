package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

public interface InvoiceService {
    Mono<Invoice> create(InvoiceDto dto);
    Mono<Invoice> update(String id, InvoiceDto dto);
    Mono<Invoice> findById(String id);
    Flux<Invoice> findPaginated(Optional<Invoice.Type> type, Optional<String> month, int page, int pageSize,
                                OrderType order);
    Mono<Boolean> deleteById(String id);
}
