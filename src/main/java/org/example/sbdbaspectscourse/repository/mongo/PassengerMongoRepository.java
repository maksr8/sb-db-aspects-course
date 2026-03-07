package org.example.sbdbaspectscourse.repository.mongo;

import org.example.sbdbaspectscourse.model.mongo.Passenger;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PassengerMongoRepository extends MongoRepository<Passenger, String> {
    Passenger findByEmail(String email);

    @Query("{ 'firstName': ?0, 'lastName': ?1 }")
    List<Passenger> findByFirstAndLastNameCustom(String firstName, String lastName);
}