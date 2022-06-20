package co.ke.proaktivio.qwanguapi.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class ReactiveTransactionConfig {

    @Bean
    @Qualifier("reactiveMongoTransactionManager")
    public ReactiveMongoTransactionManager reactiveMongoTransactionManager(ReactiveMongoDatabaseFactory rmdf) {
        return new ReactiveMongoTransactionManager(rmdf);
    }

    @Bean
    public TransactionalOperator transactionalOperator(@Qualifier("reactiveMongoTransactionManager") ReactiveMongoTransactionManager rmtm) {
        return TransactionalOperator.create(rmtm);
    }
}
