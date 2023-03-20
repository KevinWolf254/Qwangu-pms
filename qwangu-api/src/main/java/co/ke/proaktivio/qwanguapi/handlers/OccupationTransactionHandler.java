package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class OccupationTransactionHandler {
    private final OccupationTransactionService occupationTransactionService;

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("occupationTransactionId");
        return occupationTransactionService.findById(id)
        		.switchIfEmpty(Mono.error(
						new CustomNotFoundException("Occupation transaction with id %s does not exist!".formatted(id))))
				.flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Occupation Transaction found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying Occupation Transaction by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> occupationIdOptional = request.queryParam("occupationId");
        Optional<String> type = request.queryParam("type");
        Optional<String> invoiceIdOptional = request.queryParam("invoiceId");
        Optional<String> receiptIdOptional = request.queryParam("receiptId");
        Optional<String> orderOptional = request.queryParam("order");
        if (type.isPresent() &&  !EnumUtils.isValidEnum(OccupationTransaction.Type.class, type.get())) {
            String[] arrayOfState = Stream.of(OccupationTransaction.Type.values()).map(OccupationTransaction.Type::getType).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Type should be " + states + "!");
        }
        
        ValidationUtil.vaidateOrderType(orderOptional);
        log.debug(" Received request for querying occupations");
        return occupationTransactionService.findAll(
                        type.map(OccupationTransaction.Type::valueOf).orElse(null),
                        occupationIdOptional.map(occupationId -> occupationId).orElse(null),
                        invoiceIdOptional.map(invoiceId -> invoiceId).orElse(null),
                        receiptIdOptional.map(receiptId -> receiptId).orElse(null),
                        orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} Occupation Transaction", a.size()))
                .doOnError(e -> log.error(" Failed to find Occupation Transactions. Error ", e))
                .flatMap(results ->{
                	var isEmpty = results.isEmpty();
                    return ServerResponse
                    .ok()
                    .body(Mono.just(new Response<>(
                            LocalDateTime.now().toString(),
                            request.uri().getPath(),
                            HttpStatus.OK.value(),
                            !isEmpty, 
                            isEmpty ? "Occupation Transactions with those parameters do not exist!" : "Occupation Transactions found successfully.",
                            results)), Response.class);
                	
                })
                .doOnSuccess(a -> log.debug("Sent response with status code {} for querying Occupation Transactions",
                        a.rawStatusCode()));
    }

}
