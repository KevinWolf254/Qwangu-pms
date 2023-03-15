package co.ke.proaktivio.qwanguapi.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import co.ke.proaktivio.qwanguapi.models.Authority;

public interface AuthorityRepository extends ReactiveMongoRepository<Authority, String>{

}
