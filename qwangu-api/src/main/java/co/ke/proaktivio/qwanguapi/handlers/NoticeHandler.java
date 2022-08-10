package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.*;
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
public class NoticeHandler {
    private final NoticeService noticeService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateNoticeDto.class)
                .map(validateCreateNoticeDtoFunc(new CreateNoticeDtoValidator()))
                .flatMap(noticeService::create)
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/notices/%s".formatted(created.getId())))
                                .body(Mono.just(new SuccessResponse<>(true, "Notice created successfully.", created)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UpdateNoticeDto.class)
                .map(validateUpdateNoticeDtoFunc(new UpdateNoticeDtoValidator()))
                .flatMap(dto -> noticeService.update(id, dto))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Notice updated successfully.", updated)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> isActive = request.queryParam("isActive");
        Optional<String> occupationId = request.queryParam("occupationId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");

        try {
            if (isActive.isPresent() && (!"true".equalsIgnoreCase(isActive.get()) &&
            !"false".equalsIgnoreCase(isActive.get()))) {
                throw new CustomBadRequestException("isActive should be a true or false!");
            }

            return noticeService.findPaginated(
                            id,
                            isActive.map(r -> r.equalsIgnoreCase("true")),
                            occupationId,
                            page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                            pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                            order.map(OrderType::valueOf).orElse(OrderType.DESC)
                    ).collectList()
                    .flatMap(results ->
                            ServerResponse
                                    .ok()
                                    .body(Mono.just(new SuccessResponse<>(true, "Notices found successfully.", results)), SuccessResponse.class)
                                    .log())
                    .onErrorResume(handleExceptions());
        } catch (Exception e) {
            return handleExceptions(e);
        }
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return noticeService.deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Notice with id %s deleted successfully.".formatted(id), null)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    private Function<CreateNoticeDto, CreateNoticeDto> validateCreateNoticeDtoFunc(Validator validator) {
        return createNoticeDto -> {
            Errors errors = new BeanPropertyBindingResult(createNoticeDto, CreateNoticeDto.class.getName());
            validator.validate(createNoticeDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createNoticeDto;
        };
    }

    private Function<UpdateNoticeDto, UpdateNoticeDto> validateUpdateNoticeDtoFunc(Validator validator) {
        return updateNoticeDto -> {
            Errors errors = new BeanPropertyBindingResult(updateNoticeDto, UpdateNoticeDto.class.getName());
            validator.validate(updateNoticeDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return updateNoticeDto;
        };
    }
}
