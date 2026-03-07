package org.example.sbdbaspectscourse;

import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@DataMongoTest
public abstract class AbstractMongoTestcontainersSetupTest {

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.2");

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}