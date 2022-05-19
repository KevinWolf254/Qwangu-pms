package co.ke.proaktivio.qwanguapi.configurations;

import com.mongodb.reactivestreams.client.MongoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

//@Configuration
//@RequiredArgsConstructor
public class ReactiveMongoTemplateConfiguration {

//    private final MongoClient mongoClient;
//    @Value( "${spring.data.mongodb.database}" )
//    private String databaseName;
//
//    @Bean
//    public ReactiveMongoTemplate reactiveMongoTemplate() {
//        return new ReactiveMongoTemplate(mongoClient, databaseName);
//    }
}
