package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.pojos.SuccessResponse;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static co.ke.proaktivio.qwanguapi.utils.CustomErrorUtil.handleExceptions;

@Component
@RequiredArgsConstructor
public class TokenHandler {
    private final OneTimeTokenRepository oneTimeTokenRepository;

    public Mono<ServerResponse> findToken(ServerRequest request) {
        Optional<String> tokenOpt = request.queryParam("token");
        return Mono.just(tokenOpt)
                .filter(t -> t.isPresent() && !t.get().trim().isEmpty() && !t.get().trim().isBlank())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .map(Optional::get)
                .flatMap(token -> oneTimeTokenRepository.findOne(Example.of(new OneTimeToken(token))))
                .flatMap(token ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Token was found successfully.", token)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }
}
