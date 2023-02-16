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
    Mono<Notice> findByOccupationIdAndIsActive(String occupationId, Notice.Status status);
    Flux<Notice> findPaginated(Optional<String> id, Optional<Notice.Status> status, Optional<String> occupationId,
                               OrderType order);
    Mono<Boolean> deleteById(String id);
}
