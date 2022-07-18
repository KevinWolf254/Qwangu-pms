package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.pojos.NoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface NoticeService {
    Mono<Notice> create(NoticeDto dto);
    Mono<Notice> update(String id, NoticeDto dto);
    Flux<Notice> findPaginated(Optional<String> id, Optional<Boolean> active, Optional<String> occupationId, int page, int pageSize, OrderType order);
    Mono<Boolean> deleteById(String id);
}
