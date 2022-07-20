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
//        return checkIfWhitelisted(request)
//                .flatMap(r -> r.bodyToMono(DarajaCustomerToBusinessDto.class))
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
//        return checkIfWhitelisted(request)
//                .flatMap(r -> r.bodyToMono(DarajaCustomerToBusinessDto.class))
        return request.bodyToMono(DarajaCustomerToBusinessDto.class)
                .flatMap(darajaCustomerToBusinessService::confirm)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), DarajaCustomerToBusinessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

//    private Mono<ServerRequest> checkIfWhitelisted(ServerRequest request) {
//        return Mono.just(request)
//                .doOnSuccess(System.out::println)
//                .map(ServerRequest::remoteAddress)
//                .doOnSuccess(System.out::println)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .map(InetSocketAddress::getAddress)
//                .doOnSuccess(System.out::println)
//                .filter(h -> mpesaPropertiesConfig.getWhiteListedUrls().contains(h.toString().replace("/", "")))
//                .switchIfEmpty(Mono.error(new AccessDeniedException("Unauthorised ip address!")))
//                .then(Mono.just(request));
//    }
}
