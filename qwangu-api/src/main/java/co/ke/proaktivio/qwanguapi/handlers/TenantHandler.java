package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.TenantService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.TenantDtoValidator;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class TenantHandler {
    private final TenantService tenantService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(TenantDto.class)
                .map(validateTenantDtoFunc(new TenantDtoValidator()))
                .flatMap(tenantService::create)
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/tenants/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(), true, "Tenant created successfully.",
                                        created)), Response.class));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request
                .bodyToMono(TenantDto.class)
                .map(validateTenantDtoFunc(new TenantDtoValidator()))
                .flatMap(dto -> tenantService.update(id, dto))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Tenant updated successfully.",
                                        updated)), Response.class));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> mobileNumber = request.queryParam("mobileNumber");
        Optional<String> emailAddress = request.queryParam("emailAddress");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        Integer finalPage = page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1);
        Integer finalPageSize = pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10);
        OrderType finalOrder = order.map(OrderType::valueOf).orElse(OrderType.DESC);

        return tenantService.findPaginated(
                        id,
                        mobileNumber,
                        emailAddress,
                        finalPage,
                        finalPageSize,
                        finalOrder
                ).collectList()
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Tenant found successfully.",
                                        results)), Response.class));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return tenantService.deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,
                                        "Tenant with id %s deleted successfully.".formatted(id), null)),
                                        Response.class));
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
