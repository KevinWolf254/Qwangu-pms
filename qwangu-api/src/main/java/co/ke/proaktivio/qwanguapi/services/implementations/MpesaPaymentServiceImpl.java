package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import co.ke.proaktivio.qwanguapi.models.MpesaPayment;
import co.ke.proaktivio.qwanguapi.models.MpesaPayment.MpesaPaymentType;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.MpesaPaymentRepository;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class MpesaPaymentServiceImpl implements MpesaPaymentService {
	private final MpesaPaymentRepository mpesaPaymentRepository;

	@Override
    public Mono<MpesaPaymentResponse> validate(MpesaPaymentDto dto) {
        return Mono.just(new MpesaPaymentResponse(0, "ACCEPTED"));
    }
	
	@Override
	public Mono<MpesaPaymentResponse> create(MpesaPaymentDto dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
        LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), formatter).atZone(ZoneId.of("Africa/Nairobi")).toLocalDateTime();
        
        var mpesaPayment = new MpesaPayment();
        mpesaPayment.setIsProcessed(false);
        mpesaPayment.setType(dto.getTransactionType().equals("Pay Bill") ? MpesaPaymentType.MPESA_PAY_BILL : MpesaPaymentType.MPESA_TILL);
        mpesaPayment.setTransactionId(dto.getTransactionId());
        mpesaPayment.setTransactionType(dto.getTransactionType());
        mpesaPayment.setTransactionTime(transactionTime);
        mpesaPayment.setCurrency(Unit.Currency.KES);
        mpesaPayment.setAmount(BigDecimal.valueOf(Double.parseDouble(dto.getAmount())));
        mpesaPayment.setShortCode(dto.getShortCode());
        mpesaPayment.setReferenceNo(dto.getReferenceNumber());
        mpesaPayment.setInvoiceNo(dto.getInvoiceNo());
        mpesaPayment.setBalance(dto.getAccountBalance());
        mpesaPayment.setThirdPartyId(dto.getThirdPartyId());
        mpesaPayment.setMobileNumber(dto.getMobileNumber());
        mpesaPayment.setFirstName(dto.getFirstName());
        mpesaPayment.setMiddleName(dto.getMiddleName());
        mpesaPayment.setLastName(dto.getLastName());
        
		return mpesaPaymentRepository.save(mpesaPayment)
				.doOnSuccess(payment -> log.info("Created " +payment))
				.then(Mono.just(new MpesaPaymentResponse(0, "ACCEPTED")));
	}

	@Override
	public Mono<MpesaPayment> findById(String mpesaPaymentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flux<MpesaPayment> findAll(String transactionId, String referenceNo, String shortCode, OrderType order) {
		// TODO Auto-generated method stub
		return null;
	}


}
