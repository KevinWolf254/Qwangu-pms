package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit;
import reactor.core.publisher.Mono;

public interface OccupationEmailNotificationService {

	public Mono<EmailNotification> create( Unit unit, Tenant tenant, Occupation occupation);
}
