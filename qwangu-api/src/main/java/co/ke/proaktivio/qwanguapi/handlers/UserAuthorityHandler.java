package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
import co.ke.proaktivio.qwanguapi.validators.UserAuthorityDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserAuthorityHandler {
    private final UserAuthorityService userAuthorityService;

	public Mono<ServerResponse> findAll(ServerRequest request) {
		Optional<String> nameOptional = request.queryParam("name");
		Optional<String> userRoleIdOptional = request.queryParam("userRoleId");
		Optional<String> orderOptional = request.queryParam("order");

		ValidationUtil.vaidateOrderType(orderOptional);
		log.debug("Received request for querying user authorities");
		return userAuthorityService
				.findAll(nameOptional.orElse(null), userRoleIdOptional.orElse(null),
						orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)).collectList()
				.doOnSuccess(a -> log.info(" Query request returned {} user authorities", a.size()))
				.flatMap(results -> {
					var isEmpty = results.isEmpty();
					var message = isEmpty ? "Authorities were not found!" : "Authorities found successfully.";
					return ServerResponse.ok().body(Mono.just(new Response<>(LocalDateTime.now().toString(),
							request.uri().getPath(), HttpStatus.OK.value(), !isEmpty, message, results)),
							Response.class);
				}).doOnSuccess(a -> log.debug(" Sent response with status code {} for querying authorities",
						a.rawStatusCode()));
	}

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("userAuthorityId");
        return userAuthorityService.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User authority with id %S was not found!"
                        .formatted(id))))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"User authority found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info("Sent response with status code {} for querying authority by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("userAuthorityId");
        return request
                .bodyToMono(UserAuthorityDto.class)
                .doOnSuccess(a -> log.info("Received request to update {}", a))
                .map(ValidationUtil.validateUserAuthorityDto(new UserAuthorityDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to update authority was successful"))
                .flatMap(dto -> userAuthorityService.update(id, dto))
                .doOnError(e -> log.error("Failed to update authority. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(), true,"UserAuthority updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating authority", a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("userAuthorityId");
        log.debug("Received request to user authority with id {}", id);
        return userAuthorityService.deleteById(id)
                .doOnError(e -> log.error("Failed to delete apartment. Error ", e))
                .flatMap(success -> {
                    return ServerResponse
                            .ok()
                            .body(Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    "",
                                    HttpStatus.OK.value(),
                                    success, success ? "UserAuthority with id %s was deleted successfully.".formatted(id) : 
                                    	"UserAuthority with id %s was unable to be deleted!".formatted(id),
                                    null)), Response.class);
                })
                .doOnSuccess(a -> log.debug("Sent response with status code {} for deleting apartment", a.rawStatusCode()));
    }
}
