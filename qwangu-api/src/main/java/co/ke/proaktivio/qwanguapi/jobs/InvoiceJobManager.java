package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
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
public class InvoiceJobManager {
    private final OccupationService occupationService;
    private final UnitRepository unitRepository;
    private final InvoiceService invoiceService;
    private final OccupationTransactionService occupationTransactionService;

    // TODO - ADD SEND SMS
    @Scheduled(cron = "${rent.cronToCreateInvoice}")
    void createRentInvoice() {
        createRentInvoices()
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    public Flux<Invoice> createRentInvoices() {
        return occupationService.findByStatus(List.of(Occupation.Status.CURRENT))
                .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
                        .flatMap(unit -> invoiceService.create(new InvoiceDto(Invoice.Type.RENT, LocalDate.now(),
                                unit.getRentPerMonth(), unit.getSecurityPerMonth(), unit.getGarbagePerMonth(),
                                null, occupation.getId())))
                );

    }

}
