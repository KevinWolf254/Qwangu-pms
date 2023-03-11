package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.MpesaC2BService;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class MpesaC2BHandler {
    private final MpesaC2BService mpesaC2BService;

    public Mono<ServerResponse> validate(ServerRequest request) {
        return request
        		.bodyToMono(MpesaC2BDto.class)
                .doOnSuccess(a -> log.info("Received request to validate {}", a))
                .flatMap(mpesaC2BService::validate)
                .doOnSuccess(a -> log.info("Validated successfully"))
                .doOnError(e -> log.error("Failed to validate. Error ", e))
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), MpesaC2BResponse.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for validating", a.rawStatusCode()));
    }

    public Mono<ServerResponse> confirm(ServerRequest request) {
        return request.bodyToMono(MpesaC2BDto.class)
                .doOnSuccess(a -> log.info("Received request to create {}", a))
                .flatMap(mpesaC2BService::confirm)
                .doOnSuccess(a -> log.info("Created c2b successfully"))
                .doOnError(e -> log.error("Failed to create c2b. Error ", e))
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), MpesaC2BResponse.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for creating c2b", a.rawStatusCode()));
    }
}
