package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.CreateNoticeDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.UpdateNoticeDtoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static co.ke.proaktivio.qwanguapi.utils.CustomErrorUtil.handleExceptions;

@Component
@RequiredArgsConstructor
public class DarajaCustomerToBusinessHandler {
    private final DarajaCustomerToBusinessService darajaCustomerToBusinessService;

    public Mono<ServerResponse> validate(ServerRequest request) {
        return request
                .bodyToMono(DarajaCustomerToBusinessDto.class)
                .flatMap(darajaCustomerToBusinessService::validate)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), DarajaCustomerToBusinessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> confirm(ServerRequest request) {
        return request
                .bodyToMono(DarajaCustomerToBusinessDto.class)
                .flatMap(darajaCustomerToBusinessService::confirm)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), DarajaCustomerToBusinessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }
}
