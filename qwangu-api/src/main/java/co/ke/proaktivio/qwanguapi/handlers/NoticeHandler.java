package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.CreateNoticeDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.UpdateNoticeDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class NoticeHandler {
    private final NoticeService noticeService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateNoticeDto.class)
                .doOnSuccess(a -> log.debug(" Received request to create {}", a))
                .map(validateCreateNoticeDtoFunc(new CreateNoticeDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create notice was successful"))
                .flatMap(noticeService::create)
                .doOnSuccess(a -> log.info(" Created notice for occupation {} successfully", a.getOccupationId()))
                .doOnError(e -> log.error(" Failed to create notice. Error ", e))
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/notices/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(), true, "Notice created successfully.",
                                        created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating occupation", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("noticeId");
        return request.bodyToMono(UpdateNoticeDto.class)
                .doOnSuccess(a -> log.debug(" Received request to update {}", a))
                .map(validateUpdateNoticeDtoFunc(new UpdateNoticeDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update notice was successful"))
                .flatMap(dto -> noticeService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated notice for occupation {} successfully", a.getOccupationId()))
                .doOnError(e -> log.error(" Failed to update notice. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Notice updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating notice", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("noticeId");
        Optional<String> status = request.queryParam("status");
        Optional<String> occupationId = request.queryParam("occupationId");
        Optional<String> order = request.queryParam("order");

        if (status.isPresent() && StringUtils.hasText(status.get()) && !EnumUtils.isValidEnum(Notice.Status.class, status.get())) {
            String[] arrayOfState = Stream.of(Notice.Status.values()).map(Notice.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        log.info(" Received request for querying notices");
        return noticeService.findPaginated(
                        id,
                        StringUtils.hasText(status.get()) ? status.map(Notice.Status::valueOf): Optional.empty(),
                        occupationId,
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} notices", a.size()))
                .doOnError(e -> log.error(" Failed to find Notices. Error ", e))
                .flatMap(results ->{
                    var success = !results.isEmpty();
                    var message = results.isEmpty() ? "Notices with those parameters do not exist!" : "Notices found successfully.";
                    return ServerResponse
                            .ok()
                            .body(Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    request.uri().getPath(),
                                    HttpStatus.OK.value(),success, message,
                                    results)), Response.class);
                });
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("noticeId");
        log.info(" Received request to delete notice with id {}", id);
        return noticeService.deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted user successfully"))
                .doOnError(e -> log.error(" Failed to delete notice. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                                LocalDateTime.now().toString(),
                                                request.uri().getPath(),
                                                HttpStatus.OK.value(), true,
                                                "Notice with id %s deleted successfully.".formatted(id), null)),
                                        Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting notice", a.rawStatusCode()));
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
