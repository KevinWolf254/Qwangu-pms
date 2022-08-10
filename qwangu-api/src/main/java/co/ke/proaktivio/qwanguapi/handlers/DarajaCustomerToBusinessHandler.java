package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static co.ke.proaktivio.qwanguapi.utils.CustomErrorUtil.handleExceptions;

@Component
@RequiredArgsConstructor
public class DarajaCustomerToBusinessHandler {
    private final DarajaCustomerToBusinessService darajaCustomerToBusinessService;

    public Mono<ServerResponse> validate(ServerRequest request) {
        return request.bodyToMono(DarajaCustomerToBusinessDto.class)
                .flatMap(darajaCustomerToBusinessService::validate)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), DarajaCustomerToBusinessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> confirm(ServerRequest request) {
        return request.bodyToMono(DarajaCustomerToBusinessDto.class)
                .flatMap(darajaCustomerToBusinessService::confirm)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), DarajaCustomerToBusinessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }
}
