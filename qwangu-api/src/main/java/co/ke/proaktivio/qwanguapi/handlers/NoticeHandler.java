package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
import co.ke.proaktivio.qwanguapi.validators.CreateNoticeDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.UpdateNoticeDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class NoticeHandler {
    private final NoticeService noticeService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateNoticeDto.class)
                .doOnSuccess(a -> log.debug("Create request {}", a))
                .map(ValidationUtil.validateCreateNoticeDto(new CreateNoticeDtoValidator()))
                .flatMap(noticeService::create)
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
                .doOnSuccess(a -> log.debug("Update request {}", a))
                .map(ValidationUtil.validateUpdateNoticeDto(new UpdateNoticeDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to update notice was successful"))
                .flatMap(dto -> noticeService.update(id, dto))
                .doOnError(e -> log.error("Failed to update notice. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Notice updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for updating notice", a.rawStatusCode()));
    }

	public Mono<ServerResponse> findById(ServerRequest request) {
		String id = request.pathVariable("noticeId");
		return noticeService.findById(id)
				.switchIfEmpty(
						Mono.error(new CustomNotFoundException("Notice with id %s does not exist!".formatted(id))))
				.flatMap(results -> {
					var isEmpty = results == null;
					return ServerResponse.ok()
							.body(Mono.just(new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
									HttpStatus.OK.value(), !isEmpty,
									!isEmpty ? "Notice found successfully." : "Notice could not be found!", results)),
									Response.class);
				}).doOnSuccess(a -> log.debug("Sent response with status code {} for querying invoice by id",
						a.rawStatusCode()));
	}

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> statusOptional = request.queryParam("status");
        Optional<String> occupationIdOptional = request.queryParam("occupationId");
        Optional<String> orderOptional = request.queryParam("order");

        if (statusOptional.isPresent() && StringUtils.hasText(statusOptional.get()) && !EnumUtils.isValidEnum(Notice.Status.class, statusOptional.get())) {
            String[] arrayOfState = Stream.of(Notice.Status.values()).map(Notice.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        ValidationUtil.vaidateOrderType(orderOptional);
        log.debug("Received request for querying notices");
        return noticeService.findAll(
                        statusOptional.map(Notice.Status::valueOf).orElse(null),
                        occupationIdOptional.orElse(null),
                        orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info("Found {} notices", a.size()))
                .doOnError(e -> log.error("Failed to find Notices. Error ", e))
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
}
