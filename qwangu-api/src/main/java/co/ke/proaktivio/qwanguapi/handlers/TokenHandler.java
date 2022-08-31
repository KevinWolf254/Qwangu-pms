package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Example;
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
public class TokenHandler {
    private final OneTimeTokenRepository oneTimeTokenRepository;

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> tokenOpt = request.queryParam("token");
        log.info(" Received request for querying tokens");
        return Mono.just(tokenOpt)
                .filter(t -> t.isPresent() && !t.get().trim().isEmpty() && !t.get().trim().isBlank())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .map(Optional::get)
                .flatMap(token -> oneTimeTokenRepository.findOne(Example.of(new OneTimeToken(token))))
                .doOnSuccess(a -> log.info(" Query request was successful"))
                .doOnError(e -> log.error(" Failed to find token. Error ", e))
                .flatMap(token ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Token was found successfully.",
                                        token)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying token", a.rawStatusCode()));
    }
}
