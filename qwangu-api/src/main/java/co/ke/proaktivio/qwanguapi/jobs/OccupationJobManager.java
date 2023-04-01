package co.ke.proaktivio.qwanguapi.jobs;

import java.time.LocalDate;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Occupation.Status;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationEmailNotificationService;
import co.ke.proaktivio.qwanguapi.models.Unit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class OccupationJobManager {
	private final ReactiveMongoTemplate template;
	private final UnitRepository unitRepository;
	private final OccupationRepository occupationRepository;
	private final OccupationEmailNotificationService occupationEmailNotificationService;

	@Scheduled(cron = "0 0 8 * * ?")
	void process() {
		processPendingOccupations().subscribe();
	}

	public Flux<Occupation> processPendingOccupations() {
		return findByStartDateAndStatus(LocalDate.now(), Status.PENDING_OCCUPATION)
				.flatMap(occupation -> findById(occupation.getUnitId(), Unit.Status.VACANT).map(unit -> {
					unit.setStatus(Unit.Status.OCCUPIED);
					return unit;
				}).flatMap(unitRepository::save).doOnSuccess(o -> log.info("Updated: {}", o)).flatMap(unit -> {
					occupation.setStatus(Occupation.Status.CURRENT);
					return occupationRepository.save(occupation).doOnNext(o -> log.info("Updated: {}", o))
							.flatMap(o -> {
								return findTenantById(o.getTenantId()).flatMap(
										tenant -> occupationEmailNotificationService.create(unit, tenant, occupation));
							});
				}).map($ -> occupation));
	}

	private Flux<Occupation> findByStartDateAndStatus(LocalDate startDate, Occupation.Status status) {
		return template
				.find(new Query().addCriteria(Criteria.where("startDate").is(startDate).and("status").is(status))
						.with(Sort.by(Sort.Direction.DESC, "createdOn")), Occupation.class)
				.doOnNext(o -> log.info("Found: {}", o));
	}

	private Mono<Unit> findById(String unitId, Unit.Status status) {
		return template
				.findOne(new Query().addCriteria(Criteria.where("id").is(unitId).and("status").is(status)), Unit.class)
				.doOnSuccess(o -> log.info("Found: {}", o));
	}

	private Mono<Tenant> findTenantById(String tenantId) {
		return template.findOne(new Query().addCriteria(Criteria.where("id").is(tenantId)), Tenant.class)
				.doOnSuccess(o -> log.info("Found: {}", o));
	}
}
