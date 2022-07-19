package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.DarajaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.DarajaAuthenticationSuccessResponse;
import co.ke.proaktivio.qwanguapi.services.DarajaAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DarajaAuthenticationServiceImpl implements DarajaAuthenticationService {
    private final WebClient client;
    private final DarajaPropertiesConfig darajaPropertiesConfig;

    @Override
    public Mono<DarajaAuthenticationSuccessResponse> authenticate() {
        return client.get()
                .uri(darajaPropertiesConfig.getUrls().get(0))
                .headers(headers -> headers.setBasicAuth(darajaPropertiesConfig.getBasicAuthentication().getConsumerKey(),
                        darajaPropertiesConfig.getBasicAuthentication().getConsumerSecret()))
                .retrieve()
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        response -> response.bodyToMono(String.class).map(CustomBadRequestException::new))
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        response -> response.bodyToMono(String.class).map(CustomBadRequestException::new))
                .bodyToMono(DarajaAuthenticationSuccessResponse.class);
    }
}
