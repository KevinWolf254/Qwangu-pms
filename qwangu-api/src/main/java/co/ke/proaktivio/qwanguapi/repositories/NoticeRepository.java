package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Notice;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface NoticeRepository extends ReactiveMongoRepository<Notice, String> {
}
