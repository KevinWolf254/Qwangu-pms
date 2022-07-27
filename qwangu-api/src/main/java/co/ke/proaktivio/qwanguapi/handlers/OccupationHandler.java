package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.CreateOccupationDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.UpdateOccupationDtoValidator;
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
public class OccupationHandler {
    private final OccupationService occupationService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(CreateOccupationDto.class)
                .map(validateCreateOccupationDtoFunc(new CreateOccupationDtoValidator()))
                .flatMap(occupationService::create)
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/users/%s".formatted(created.getId())))
                        .body(Mono.just(new SuccessResponse<>(true, "Occupation created successfully.", created)), SuccessResponse.class)
                        .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request
                .bodyToMono(UpdateOccupationDto.class)
                .map(validateUpdateOccupationDtoFunc(new UpdateOccupationDtoValidator()))
                .flatMap(dto -> occupationService.update(id, dto))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Occupation updated successfully.", updated)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> status = request.queryParam("status");
        Optional<String> unitId = request.queryParam("unitId");
        Optional<String> tenantId = request.queryParam("tenantId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        try {
            if(status.isPresent() && !status.get().equals("CURRENT") && !status.get().equals("MOVED"))
                throw new CustomBadRequestException("Status should be CURRENT or MOVED!");

            return occupationService.findPaginated(
                        id,
                        status.map(Occupation.Status::valueOf),
                        unitId,
                        tenantId,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Occupations found successfully.", results)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
        } catch (Exception e) {
            return handleExceptions(e);
        }
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return occupationService
                .deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Occupation with id %s deleted successfully."
                                        .formatted(id), null)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    private Function<CreateOccupationDto, CreateOccupationDto> validateCreateOccupationDtoFunc(Validator validator) {
        return createOccupationDto -> {
            Errors errors = new BeanPropertyBindingResult(createOccupationDto, CreateOccupationDto.class.getName());
            validator.validate(createOccupationDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createOccupationDto;
        };
    }

    private Function<UpdateOccupationDto, UpdateOccupationDto> validateUpdateOccupationDtoFunc(Validator validator) {
        return updateOccupationDto -> {
            Errors errors = new BeanPropertyBindingResult(updateOccupationDto, UpdateOccupationDto.class.getName());
            validator.validate(updateOccupationDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return updateOccupationDto;
        };
    }
}
