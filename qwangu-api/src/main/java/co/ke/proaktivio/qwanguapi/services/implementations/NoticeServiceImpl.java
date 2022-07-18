package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.pojos.NoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.NoticeRepository;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;

    @Override
    public Mono<Notice> create(NoticeDto dto) {
        return null;
    }

    @Override
    public Mono<Notice> update(String id, NoticeDto dto) {
        return null;
    }

    @Override
    public Flux<Notice> findPaginated(Optional<String> id, Optional<Boolean> active, Optional<String> occupationId,
                                      int page, int pageSize, OrderType order) {
        return null;
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return null;
    }
}
