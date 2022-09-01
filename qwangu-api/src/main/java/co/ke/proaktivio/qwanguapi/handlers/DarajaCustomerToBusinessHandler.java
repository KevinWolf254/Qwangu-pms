package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class DarajaCustomerToBusinessHandler {
    private final DarajaCustomerToBusinessService darajaCustomerToBusinessService;

    public Mono<ServerResponse> validate(ServerRequest request) {
        return request.bodyToMono(DarajaCustomerToBusinessDto.class)
                .doOnSuccess(a -> log.info(" Received request to validate {}", a))
                .flatMap(darajaCustomerToBusinessService::validate)
                .doOnSuccess(a -> log.info(" Validated successfully"))
                .doOnError(e -> log.error(" Failed to validate. Error ", e))
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), DarajaCustomerToBusinessResponse.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for validating", a.rawStatusCode()));
    }

    public Mono<ServerResponse> confirm(ServerRequest request) {
        return request.bodyToMono(DarajaCustomerToBusinessDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .flatMap(darajaCustomerToBusinessService::confirm)
                .doOnSuccess(a -> log.info(" Created c2b successfully"))
                .doOnError(e -> log.error(" Failed to create c2b. Error ", e))
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), DarajaCustomerToBusinessResponse.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating c2b", a.rawStatusCode()));
    }
}
