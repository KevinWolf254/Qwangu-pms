package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Receipt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ReceiptRepository extends ReactiveMongoRepository<Receipt, String> {
}
