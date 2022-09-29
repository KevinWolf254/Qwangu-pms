package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.ReceivableDto;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import co.ke.proaktivio.qwanguapi.services.ReceivableService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReceivableJobManager {
    private final OccupationService occupationService;
    private final UnitRepository unitRepository;
    private final ReceivableService receivableService;
    private final OccupationTransactionService occupationTransactionService;

    // TODO - ADD SEND SMS
    @Scheduled(cron = "${rent.cronToCreateInvoice}")
    void createRentInvoice() {
        createRentInvoices()
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    public Flux<OccupationTransaction> createRentInvoices() {
        return occupationService
                .findByStatus(List.of(Occupation.Status.CURRENT, Occupation.Status.BOOKED))
                .doOnNext(t -> System.out.println("---- Found: " +t))
                .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
                        .doOnSuccess(t -> System.out.println("---- Found: " +t))
                        .flatMap(unit -> occupationTransactionService.findLatestByOccupationId(occupation.getId())
                                .switchIfEmpty(Mono.just(new OccupationTransaction(null, null, BigDecimal.ZERO,
                                        BigDecimal.ZERO, BigDecimal.ZERO, occupation.getId(), null,"1")))
                                .doOnSuccess(t -> System.out.println("---- Found: " +t))
                                .flatMap(previousOccupationTransaction -> receivableService
                                        .create(new ReceivableDto(Receivable.Type.RENT, LocalDate.now(),
                                                unit.getRentPerMonth(), unit.getSecurityPerMonth(), unit.getGarbagePerMonth(),
                                                null))
                                        .doOnSuccess(t -> System.out.println("---- Created: " +t))
                                        .flatMap(receivable -> {
                                            BigDecimal rentSecurityGarbage = unit.getRentPerMonth()
                                                    .add(unit.getSecurityPerMonth()).add(unit.getGarbagePerMonth());
                                            BigDecimal totalCarriedForward = rentSecurityGarbage.add(
                                                    previousOccupationTransaction.getTotalAmountCarriedForward());
                                            return occupationTransactionService.create(
                                                    new OccupationTransactionDto(OccupationTransaction.Type.DEBIT,
                                                            rentSecurityGarbage, BigDecimal.ZERO, totalCarriedForward,
                                                            occupation.getId(), receivable.getId(), null));
                                        })
                                        .doOnSuccess(t -> System.out.println("---- Created: " +t))
                                )
                        )
                );

    }

}
