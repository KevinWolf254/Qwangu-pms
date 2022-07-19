package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.pojos.DarajaAuthenticationSuccessResponse;
import reactor.core.publisher.Mono;

public interface DarajaAuthenticationService {
    Mono<DarajaAuthenticationSuccessResponse> authenticate();
}
