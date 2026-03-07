package org.example.sbdbaspectscourse.repository.mongo;

import org.example.sbdbaspectscourse.model.mongo.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DriverMongoRepository extends MongoRepository<Driver, String> {
    Driver findByLicenseNumber(String licenseNumber);

    @Query("{ 'rating': { $gte: ?0 }, 'active': true }")
    List<Driver> findActiveDriversWithRatingGreaterThanEqual(Double minRating);
}