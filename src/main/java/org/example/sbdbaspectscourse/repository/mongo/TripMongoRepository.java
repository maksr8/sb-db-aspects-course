package org.example.sbdbaspectscourse.repository.mongo;

import org.example.sbdbaspectscourse.model.mongo.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TripMongoRepository extends MongoRepository<Trip, String> {
    List<Trip> findAllByStatus(String status);

    @Query("{ 'cost': { $lt: ?0 }, 'status': ?1 }")
    List<Trip> findCheapTripsByStatus(Double maxCost, String status);
}