package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceJobManager {
	private final OccupationService occupationService;
	private final InvoiceService invoiceService;
	// TODO - CREATE JOB TO PROCESS OVERDUE PAYMENTS (PENALTIES - PERCENTAGE OF RENT E.G. 0.08) AND CREATE NOTIFICATION

	@Scheduled(cron = "${rent.cronToCreateInvoice}")
	void createRentInvoice() {
		createRentInvoices().subscribe();
	}

	public Flux<Invoice> createRentInvoices() {
		var today = LocalDate.now();
		var firstDay = today.withDayOfMonth(1);
		var lastDay = firstDay.with(lastDayOfMonth());
		
		return occupationService.findByStatus(List.of(Occupation.Status.CURRENT))
				.flatMap(occupation -> invoiceService.create(
						new InvoiceDto(Invoice.Type.RENT, firstDay, lastDay, Currency.KES, null, occupation.getId()))
				);

	}

}
