package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TenantService {
    Mono<Tenant> create(TenantDto dto);
    Mono<Tenant> update(String tenantId, TenantDto dto);
    Mono<Tenant> findTenantByMobileNumber(String mobileNumber);
    Mono<Tenant> findById(String tenantId);
    Flux<Tenant> findAll(String mobileNumber, String emailAddress, OrderType order);
    Mono<Boolean> deleteById(String id);

}
