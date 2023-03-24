package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateNoticeDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NoticeService {
    Mono<Notice> create(CreateNoticeDto dto);
    Mono<Notice> update(String id, UpdateNoticeDto dto);
    Mono<Notice> findById(String noticeId);
    Mono<Notice> findByOccupationIdAndIsActive(String occupationId, Notice.Status status);
    Flux<Notice> findAll(Notice.Status status, String occupationId, OrderType order);
    Mono<Boolean> deleteById(String id);
}
