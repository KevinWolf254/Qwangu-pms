package co.ke.proaktivio.qwanguapi.jobs;

import java.util.Locale;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.ke.proaktivio.qwanguapi.configs.SmsMessageSourceConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.NotificationsPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.pojos.SmsNotificationDto;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import co.ke.proaktivio.qwanguapi.services.ReceiptService;
import co.ke.proaktivio.qwanguapi.services.SmsNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class PaymentJobManager {
	private final NotificationsPropertiesConfig config;
	private final PaymentService paymentService;
	private final SmsNotificationService smsNotificationService;
	private final MpesaPaymentService mpesaPaymentService;
	private final OccupationService occupationService;
	private final ReceiptService receiptService;
	private final SmsMessageSourceConfig messagesConfig;

	@Scheduled(cron = "0 0/10 * * * ?")
	void process() {
		processMobilePayments().subscribe();
	}

	@Transactional
	public Flux<Payment> processMobilePayments() {
		return paymentService.findAll(PaymentStatus.UNCLAIMED, PaymentType.MOBILE, null, null)
				.doOnNext(n -> log.info("Found " + n))
				.flatMap(payment -> {
					var houseNumber = payment.getOccupationNumber();
					return occupationService.findByNumber(houseNumber).doOnSuccess(n -> log.info("Found " + n))
							.map(occupation -> {
								var dto = new ReceiptDto();
								dto.setPaymentId(payment.getId());
								dto.setOccupationId(occupation.getId());
								return dto;
							}).flatMap(receiptService::create).map($ -> payment);
				}).map(payment -> {
					payment.setStatus(PaymentStatus.CLAIMED);
					return payment;
				}).flatMap(paymentService::update)
				.flatMap(payment -> {
					var referenceNumber = payment.getReferenceNumber();
					var houseNumber = payment.getOccupationNumber();

					return Mono.just(config.isSendSms()).filter(canSendSms -> canSendSms)
							.flatMap($ -> mpesaPaymentService.findByTransactionId(payment.getReferenceNumber()))
							.map(mpesa -> {
								var amount = payment.getCurrency().name().toLowerCase().concat(" ")
										.concat(payment.getAmount().toPlainString());
								var message = messagesConfig.smsMessageSource().getMessage(
										"sms.notification.payment.mobile",
										new Object[] { amount, referenceNumber, houseNumber }, Locale.getDefault());

								var dto = new SmsNotificationDto();
								dto.setPhoneNumber(mpesa.getMobileNumber());
								dto.setMessage(message);
								return dto;
							}).flatMap(smsNotificationService::create).then(Mono.just(payment));
				}).map(payment -> {
					
					return payment;
				}).doOnError(e -> log.error("Error occurred: " + e));
	}
}
