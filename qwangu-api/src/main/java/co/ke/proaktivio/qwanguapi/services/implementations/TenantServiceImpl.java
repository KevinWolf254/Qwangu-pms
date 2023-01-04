package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
import co.ke.proaktivio.qwanguapi.services.TenantService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    private final TenantRepository tenantRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Tenant> create(TenantDto dto) {
        String mobileNumber = dto.getMobileNumber();
        String emailAddress = dto.getEmailAddress();
        Query query = new Query()
                .addCriteria(Criteria
                        .where("mobileNumber").is(mobileNumber)
                        .orOperator(Criteria
                                .where("emailAddress").is(emailAddress))
                );
        return template
                .exists(query, Tenant.class)
                .filter(exits -> !exits)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Tenant already exists!")))
                .then(Mono.just(
                        new Tenant.TenantBuilder()
                                .firstName(dto.getFirstName())
                                .middleName(dto.getMiddleName())
                                .surname(dto.getSurname())
                                .mobileNumber(dto.getMobileNumber())
                                .emailAddress(dto.getEmailAddress())
                                .build()
                ))
                .flatMap(tenantRepository::save)
                .doOnSuccess(tenant -> log.info(" Created " +tenant));
    }

    @Override
    public Mono<Tenant> update(String id, TenantDto dto) {
        return Mono.just(dto)
                .map(d -> {
                    String mobileNumber = d.getMobileNumber();
                    String emailAddress = d.getEmailAddress();
                    return new Query()
                            .addCriteria(Criteria
                                    .where("mobileNumber").is(mobileNumber)
                                    .orOperator(Criteria
                                            .where("emailAddress").is(emailAddress))
                                    .and("id").nin(id)
                            );
                })
                .flatMap(q -> template.exists(q, Tenant.class))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Tenant already exists!")))
                .then(tenantRepository.findById(id))
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Tenant with id %s does not exist!".formatted(id))))
                .map(tenant -> {
                    if(tenant.getFirstName() !=null && !tenant.getFirstName().trim().isEmpty() &&
                            !tenant.getFirstName().trim().isBlank())
                        tenant.setFirstName(dto.getFirstName());
                    if(tenant.getMiddleName() !=null && !tenant.getMiddleName().trim().isEmpty() &&
                            !tenant.getMiddleName().trim().isBlank())
                        tenant.setMiddleName(dto.getMiddleName());
                    if(tenant.getSurname() !=null && !tenant.getSurname().trim().isEmpty() &&
                            !tenant.getSurname().trim().isBlank())
                        tenant.setSurname(dto.getSurname());
                    if(tenant.getEmailAddress() !=null && !tenant.getEmailAddress().trim().isEmpty() &&
                    !tenant.getEmailAddress().trim().isBlank())
                        tenant.setEmailAddress(dto.getEmailAddress());
                    if(tenant.getMobileNumber() !=null && !tenant.getMobileNumber().trim().isEmpty() &&
                            !tenant.getMobileNumber().trim().isBlank())
                        tenant.setMobileNumber(dto.getMobileNumber());
                    tenant.setModifiedOn(LocalDateTime.now());
                    return tenant;
                })
                .flatMap(tenantRepository::save);
    }

    @Override
    public Mono<Tenant> findTenantByMobileNumber(String mobileNumber) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("mobileNumber").is(mobileNumber)), Tenant.class);
    }

    @Override
    public Mono<Tenant> findById(String tenantId) {
        return tenantRepository.findById(tenantId);
    }

    @Override
    public Flux<Tenant> findPaginated(Optional<String> mobileNumber, Optional<String> emailAddress, OrderType order) {
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        mobileNumber.ifPresent(mobile -> {
            if(StringUtils.hasText(mobile))
                query.addCriteria(Criteria.where("mobileNumber").regex(".*" +mobile.trim()+ ".*", "i"));
        });
        emailAddress.ifPresent(email -> {
            if(StringUtils.hasText(email))
                query.addCriteria(Criteria.where("emailAddress").regex(".*" +email.trim()+ ".*", "i"));
        });
        query.with(sort);
        return template
                .find(query, Tenant.class);
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Tenant.class)
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
