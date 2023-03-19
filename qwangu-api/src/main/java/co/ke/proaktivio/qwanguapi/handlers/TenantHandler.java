package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.TenantService;
import co.ke.proaktivio.qwanguapi.validators.TenantDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class TenantHandler {
    private final TenantService tenantService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(TenantDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(ValidationUtil.validateTenantDtoFunc(new TenantDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create tenant was successful"))
                .flatMap(tenantService::create)
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/tenants/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(), true, "Tenant created successfully.",
                                        created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating tenant", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("tenantId");
        return tenantService.findById(id)
        		.switchIfEmpty(Mono.error(new CustomNotFoundException("Tenant with id %s was not found!".formatted(id))))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Tenant found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying tenant by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("tenantId");
        return request.bodyToMono(TenantDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
                .map(ValidationUtil.validateTenantDtoFunc(new TenantDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update tenant was successful"))
                .flatMap(dto -> tenantService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated user {} successfully", a.getEmailAddress()))
                .doOnError(e -> log.error(" Failed to update user. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Tenant updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating tenant", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> mobileNumberOptional = request.queryParam("mobileNumber");
        Optional<String> emailAddressOptional = request.queryParam("emailAddress");
        Optional<String> orderOptional = request.queryParam("order");
        
        ValidationUtil.vaidateOrderType(orderOptional);
        log.info(" Received request for querying tenants");
        return tenantService.findAll(
                        mobileNumberOptional.orElse(null),
                        emailAddressOptional.orElse(null),
                        orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} tenants", a.size()))
                .doOnError(e -> log.error(" Failed to find tenants. Error ", e))
                .flatMap(results -> {
                    boolean isEmpty = results.isEmpty();
                    return ServerResponse
                            .ok()
                            .body(Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    request.uri().getPath(),
                                    HttpStatus.OK.value(), !isEmpty, !isEmpty ? "Tenant found successfully." : "Tenants with those parameters do not exist!",
                                    results)), Response.class);
                })
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying tenants", a.rawStatusCode()));
    }
}
