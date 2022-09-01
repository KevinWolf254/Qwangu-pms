package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.TenantService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.TenantDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class TenantHandler {
    private final TenantService tenantService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(TenantDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(validateTenantDtoFunc(new TenantDtoValidator()))
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

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("tenantId");
        return request
                .bodyToMono(TenantDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
                .map(validateTenantDtoFunc(new TenantDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update user was successful"))
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

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("tenantId");
        Optional<String> mobileNumber = request.queryParam("mobileNumber");
        Optional<String> emailAddress = request.queryParam("emailAddress");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        Integer finalPage = page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1);
        Integer finalPageSize = pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10);
        OrderType finalOrder = order.map(OrderType::valueOf).orElse(OrderType.DESC);
        log.info(" Received request for querying tenants");
        return tenantService.findPaginated(
                        id,
                        mobileNumber,
                        emailAddress,
                        finalPage,
                        finalPageSize,
                        finalOrder
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} tenants", a.size()))
                .doOnError(e -> log.error(" Failed to find tenants. Error ", e))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Tenant found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying tenants", a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("tenantId");
        log.info(" Received request to delete tenant with id {}", id);
        return tenantService.deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted user successfully"))
                .doOnError(e -> log.error(" Failed to delete user. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,
                                        "Tenant with id %s deleted successfully.".formatted(id), null)),
                                        Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting tenant", a.rawStatusCode()));
    }


    private Function<TenantDto, TenantDto> validateTenantDtoFunc(Validator validator) {
        return tenantDto -> {
            Errors errors = new BeanPropertyBindingResult(tenantDto, TenantDto.class.getName());
            validator.validate(tenantDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return tenantDto;
        };
    }
}
