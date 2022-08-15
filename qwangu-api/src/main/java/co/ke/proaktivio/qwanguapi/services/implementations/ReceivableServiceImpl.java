package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.pojos.ReceivableDto;
import co.ke.proaktivio.qwanguapi.repositories.ReceivableRepository;
import co.ke.proaktivio.qwanguapi.services.ReceivableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReceivableServiceImpl implements ReceivableService {
    private final ReceivableRepository receivableRepository;

    @Override
    public Mono<Receivable> create(ReceivableDto dto) {
        return receivableRepository.save(new Receivable(null, dto.getType(), dto.getPeriod(), dto.getRentAmount(),
                dto.getSecurityAmount(), dto.getGarbageAmount(), dto.getOtherAmounts(), LocalDateTime.now(), null));
    }
}
