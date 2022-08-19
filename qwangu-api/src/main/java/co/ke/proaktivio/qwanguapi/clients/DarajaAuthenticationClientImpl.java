package co.ke.proaktivio.qwanguapi.clients;

import co.ke.proaktivio.qwanguapi.pojos.DarajaAuthenticationSuccessResponse;
import reactor.core.publisher.Mono;

//@Service
//@RequiredArgsConstructor
public class DarajaAuthenticationClientImpl implements DarajaAuthenticationClient {
//    private final WebClient client;
//    private final MpesaPropertiesConfig mpesaPropertiesConfig;
//
    @Override
    public Mono<DarajaAuthenticationSuccessResponse> authenticate() {
//        return client.get()
//                .uri(mpesaPropertiesConfig.getDaraja().getUrls().get(0))
//                .headers(headers -> headers.setBasicAuth(mpesaPropertiesConfig.getDaraja().getBasicAuthentication().getConsumerKey(),
//                        mpesaPropertiesConfig.getDaraja().getBasicAuthentication().getConsumerSecret()))
//                .retrieve()
//                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
//                        response -> response.bodyToMono(String.class).map(CustomBadRequestException::new))
//                .onStatus(HttpStatus.BAD_REQUEST::equals,
//                        response -> response.bodyToMono(String.class).map(CustomBadRequestException::new))
//                .bodyToMono(DarajaAuthenticationSuccessResponse.class);
        return null;
    }
}
