package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateNoticeDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface NoticeService {
    Mono<Notice> create(CreateNoticeDto dto);
    Mono<Notice> update(String id, UpdateNoticeDto dto);
    Flux<Notice> findPaginated(Optional<String> id, Optional<Boolean> isActive, Optional<String> occupationId, int page, int pageSize, OrderType order);
    Mono<Boolean> deleteById(String id);
}
