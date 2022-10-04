package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.repositories.ReceiptRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import co.ke.proaktivio.qwanguapi.services.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {
    private final OccupationService occupationService;
    private final PaymentService paymentService;
    private final ReceiptRepository receiptRepository;

    @Override
    public Mono<Receipt> create(ReceiptDto dto) {
        return occupationService.findById(dto.getOccupationId())
                .doOnSuccess(occupation -> log.info("Found {}", occupation))
                .flatMap($ -> paymentService.findById(dto.getPaymentId()))
                .doOnSuccess(payment -> log.info("Found {}", payment))
                .flatMap($ -> receiptRepository.save(new Receipt(dto.getOccupationId(), dto.getPaymentId())));
    }

    @Override
    public Mono<Receipt> findById(String id) {
        return null;
    }

    @Override
    public Flux<Receipt> findPaginated(Optional<String> occupationId, Optional<String> paymentId, int page, int pageSize, OrderType order) {
        return null;
    }
}
