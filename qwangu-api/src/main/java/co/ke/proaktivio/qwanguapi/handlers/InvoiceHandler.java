package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class InvoiceHandler {
    private final InvoiceService invoiceService;

}
