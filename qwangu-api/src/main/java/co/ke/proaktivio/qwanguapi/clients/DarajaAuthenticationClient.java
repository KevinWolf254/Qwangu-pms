package co.ke.proaktivio.qwanguapi.clients;

import co.ke.proaktivio.qwanguapi.pojos.DarajaAuthenticationSuccessResponse;
import reactor.core.publisher.Mono;

public interface DarajaAuthenticationClient {
    Mono<DarajaAuthenticationSuccessResponse> authenticate();
}
