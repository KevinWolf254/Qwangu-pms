package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import reactor.core.publisher.Mono;

public interface DarajaCustomerToBusinessService {
    Mono<DarajaCustomerToBusinessResponse> validate(DarajaCustomerToBusinessDto dto);
    Mono<DarajaCustomerToBusinessResponse> confirm(DarajaCustomerToBusinessDto dto);
    Mono<Payment> processBooking(Payment payment);
    Mono<Payment> processPayment(Payment payment);
    Mono<Payment> processRentAdvance(Payment payment);
}
