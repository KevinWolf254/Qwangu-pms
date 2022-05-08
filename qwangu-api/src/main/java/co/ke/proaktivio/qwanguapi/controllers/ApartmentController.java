package co.ke.proaktivio.qwanguapi.controllers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping(value = "v1/apartments")
@RequiredArgsConstructor
public class ApartmentController {
    private final ApartmentService apartmentService;

    @PostMapping
    public Mono<Apartment> create(@RequestBody ApartmentDto dto) {
        return apartmentService.create(dto);
    }

    @PutMapping("/{id}")
    public Mono<Apartment> update(@PathVariable String id,
                                  @RequestBody ApartmentDto dto) {
        return apartmentService.update(id, dto);
    }

    @GetMapping
    public Flux<Apartment> find(@RequestParam(required = false) String id,
                                @RequestParam(required = false) String name,
                                @RequestParam String page,
                                @RequestParam String pageSize,
                                @RequestParam OrderType order) {
        Optional<String> optionalId = CustomUtils.convertToOptional(id);
        Optional<String> optionalName = CustomUtils.convertToOptional(name);
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");

        return apartmentService.findPaginated(
                optionalId,
                optionalName,
                finalPage - 1,
                finalPageSize,
                order
        );
    }

    @DeleteMapping("/{id}")
    public Mono<String> deleteById(@PathVariable String id) {
        return apartmentService.deleteById(id);
    }
}
