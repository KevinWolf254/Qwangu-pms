package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Receipt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends ReactiveMongoRepository<Receipt, String> {
}