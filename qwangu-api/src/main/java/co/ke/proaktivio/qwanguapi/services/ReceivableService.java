package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.pojos.ReceivableDto;
import reactor.core.publisher.Mono;

public interface ReceivableService {
    Mono<Receivable> create(ReceivableDto dto);
}
