package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.NoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateNoticeDto;
import co.ke.proaktivio.qwanguapi.repositories.NoticeRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final OccupationRepository occupationRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Notice> create(CreateNoticeDto dto) {
        return Mono.just(dto)
                .flatMap(d -> occupationRepository.findById(d.getOccupationId()))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(dto.getOccupationId()))))
                .filter(Occupation::getActive)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not create notice of occupation that is not active!")))
                .then(Mono.just(new Notice(null, true, dto.getNotificationDate(), dto.getVacatingDate(), LocalDateTime.now(), null, dto.getOccupationId())))
                .flatMap(noticeRepository::save);
    }

    @Override
    public Mono<Notice> update(String id, UpdateNoticeDto dto) {
        return noticeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Notice with id %s does not exist!".formatted(id))))
                .flatMap(notice -> occupationRepository
                        .findById(notice.getOccupationId())
                        .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(notice.getOccupationId()))))
                        .filter(Occupation::getActive)
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not create notice of occupation that is not active!")))
                        .then(Mono.just(notice)))
                .map(notice -> {
                    if (dto.getActive() != null)
                        notice.setActive(dto.getActive());
                    if (dto.getNotificationDate() != null)
                        notice.setNotificationDate(dto.getNotificationDate());
                    if (dto.getVacatingDate() != null)
                        notice.setVacatingDate(dto.getVacatingDate());
                    notice.setModified(LocalDateTime.now());
                    return notice;
                })
                .flatMap(noticeRepository::save);
    }

    @Override
    public Flux<Notice> findPaginated(Optional<String> id, Optional<Boolean> active, Optional<String> occupationId,
                                      int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        active.ifPresent(isActive -> query.addCriteria(Criteria.where("active").is(isActive)));
        occupationId.ifPresent(i -> query.addCriteria(Criteria.where("occupationId").is(i)));
        query.with(pageable)
                .with(sort);
        return template
                .find(query, Notice.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Notices were not found!")));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Notice.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Notice with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
