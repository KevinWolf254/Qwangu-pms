package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OccupationTransactionServiceImpl implements OccupationTransactionService {
    private final OccupationTransactionRepository occupationTransactionRepository;

    @Override
    public Mono<OccupationTransaction> create(OccupationTransactionDto dto) {
        return occupationTransactionRepository.save(new OccupationTransaction(null, dto.getTotalAmountOwed(), dto.getTotalAmountOwed(), dto.getTotalAmountRemaining(),
                dto.getOccupationId(), dto.getReceivableId(), dto.getPaymentId(), LocalDateTime.now()));
    }
}
