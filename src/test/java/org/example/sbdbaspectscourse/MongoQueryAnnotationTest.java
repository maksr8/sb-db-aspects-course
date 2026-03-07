package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.model.mongo.Driver;
import org.example.sbdbaspectscourse.model.mongo.Passenger;
import org.example.sbdbaspectscourse.model.mongo.Trip;
import org.example.sbdbaspectscourse.repository.mongo.DriverMongoRepository;
import org.example.sbdbaspectscourse.repository.mongo.PassengerMongoRepository;
import org.example.sbdbaspectscourse.repository.mongo.TripMongoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

class MongoQueryAnnotationTest extends AbstractMongoTestcontainersSetupTest {

    @Autowired
    private DriverMongoRepository driverRepository;

    @Autowired
    private PassengerMongoRepository passengerRepository;

    @Autowired
    private TripMongoRepository tripRepository;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
        passengerRepository.deleteAll();
        tripRepository.deleteAll();
    }

    @Test
    void testFindActiveDriversWithRatingGreaterThanEqual() {
        Driver driver1 = new Driver();
        driver1.setFullName("Active Good Driver");
        driver1.setRating(4.8);
        driver1.setActive(true);
        driverRepository.save(driver1);

        Driver driver2 = new Driver();
        driver2.setFullName("Active Bad Driver");
        driver2.setRating(3.5);
        driver2.setActive(true);
        driverRepository.save(driver2);

        Driver driver3 = new Driver();
        driver3.setFullName("Inactive Good Driver");
        driver3.setRating(4.9);
        driver3.setActive(false);
        driverRepository.save(driver3);

        List<Driver> result = driverRepository.findActiveDriversWithRatingGreaterThanEqual(4.5);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Active Good Driver", result.getFirst().getFullName());
    }

    @Test
    void testFindByFirstAndLastNameCustom() {
        Passenger passenger = new Passenger();
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setEmail("john.doe@example.com");
        passengerRepository.save(passenger);

        List<Passenger> result = passengerRepository.findByFirstAndLastNameCustom("John", "Doe");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("john.doe@example.com", result.getFirst().getEmail());
    }

    @Test
    void testFindCheapTripsByStatus() {
        Trip trip1 = new Trip();
        trip1.setCost(10.0);
        trip1.setStatus("COMPLETED");
        tripRepository.save(trip1);

        Trip trip2 = new Trip();
        trip2.setCost(25.0);
        trip2.setStatus("COMPLETED");
        tripRepository.save(trip2);

        Trip trip3 = new Trip();
        trip3.setCost(8.0);
        trip3.setStatus("CANCELLED");
        tripRepository.save(trip3);

        List<Trip> result = tripRepository.findCheapTripsByStatus(15.0, "COMPLETED");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(10.0, result.getFirst().getCost());
    }
}