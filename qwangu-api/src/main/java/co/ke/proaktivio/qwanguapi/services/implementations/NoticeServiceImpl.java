package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
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
                .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not create notice of occupation that is not active!")))
                .then(Mono.just(new Notice(null, true, dto.getNotifiedOn(), dto.getVacatingOn(), LocalDateTime.now(), null, dto.getOccupationId())))
                .flatMap(noticeRepository::save);
    }

    @Override
    public Mono<Notice> update(String id, UpdateNoticeDto dto) {
        return noticeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Notice with id %s does not exist!".formatted(id))))
                .filter(Notice::getIsActive)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not update notice that is inactive!")))
                .flatMap(notice -> occupationRepository.findById(notice.getOccupationId())
                        .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(notice.getOccupationId()))))
                        .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not update notice of occupation that is not active!")))
                        .then(Mono.just(notice)))
                .map(notice -> {
                    if (dto.getIsActive() != null)
                        notice.setIsActive(dto.getIsActive());
                    if (dto.getNotifiedOn() != null)
                        notice.setNotifiedOn(dto.getNotifiedOn());
                    if (dto.getVacatingOn() != null)
                        notice.setVacatingOn(dto.getVacatingOn());
                    notice.setModifiedOn(LocalDateTime.now());
                    return notice;
                })
                .flatMap(noticeRepository::save);
    }

    @Override
    public Flux<Notice> findPaginated(Optional<String> id, Optional<Boolean> isActive, Optional<String> occupationId,
                                      int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        isActive.ifPresent(s -> query.addCriteria(Criteria.where("isActive").is(s)));
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