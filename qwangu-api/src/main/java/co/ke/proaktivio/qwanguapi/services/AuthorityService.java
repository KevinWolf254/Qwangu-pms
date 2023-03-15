package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthorityService {

	Mono<Authority> findById(String authorityId);

	Flux<Authority> findAll(String name, OrderType order);
}
