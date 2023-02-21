package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.InvoiceDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
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
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class InvoiceHandler {
    private final InvoiceService invoiceService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(InvoiceDto.class)
                .doOnSuccess(a -> log.debug(" Received request to create {}", a))
                .map(validateInvoiceDtoFunc(new InvoiceDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create invoice was successful"))
                .flatMap(invoiceService::create)
                .doOnSuccess(a -> log.info(" Created invoice for occupation {} successfully",a.getOccupationId()))
                .doOnError(e -> log.error(" Failed to create invoice. Error ", e))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/invoices/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Invoice created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating invoice", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("occupationId");
        return request
                .bodyToMono(InvoiceDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
                .map(validateInvoiceDtoFunc(new InvoiceDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update invoice was successful"))
                .flatMap(dto -> invoiceService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated invoice for occupation {}successfully", a.getOccupationId()))
                .doOnError(e -> log.error(" Failed to update occupation. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Invoice updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating invoice", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("invoiceId");
        return invoiceService.findById(id)
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Invoice found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying invoice by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> type = request.queryParam("type");
        Optional<String> month = request.queryParam("month");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        if (type.isPresent() &&  !EnumUtils.isValidEnum(Invoice.Type.class, type.get())) {
            String[] arrayOfState = Stream.of(Invoice.Type.values()).map(Invoice.Type::getName).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Type should be " + states + "!");
        }
        log.info(" Received request for querying occupations");
        return invoiceService.findPaginated(
                        type.map(Invoice.Type::valueOf),
                        month,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} occupation", a.size()))
                .doOnError(e -> log.error(" Failed to find occupations. Error ", e))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Invoices found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying invoices",
                        a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("invoiceId");
        log.info(" Received request to delete invoices with id {}", id);
        return invoiceService
                .deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted invoice successfully"))
                .doOnError(e -> log.error(" Failed to delete invoice. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Invoice with id %s deleted successfully."
                                        .formatted(id), null)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting invoice", a.rawStatusCode()));
    }

    private Function<InvoiceDto, InvoiceDto> validateInvoiceDtoFunc(Validator validator) {
        return invoiceDto -> {
            Errors errors = new BeanPropertyBindingResult(invoiceDto, InvoiceDto.class.getName());
            validator.validate(invoiceDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return invoiceDto;
        };
    }
}
