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

class MongoDerivedQueryTest extends AbstractMongoTestcontainersSetupTest {

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
    void testFindByLicenseNumber() {
        Driver driver = new Driver();
        driver.setFullName("John Doe");
        driver.setLicenseNumber("LIC-12345");
        driver.setRating(4.8);
        driver.setActive(true);
        driverRepository.save(driver);

        Driver found = driverRepository.findByLicenseNumber("LIC-12345");

        Assertions.assertNotNull(found);
        Assertions.assertEquals("John Doe", found.getFullName());
    }

    @Test
    void testFindByEmail() {
        Passenger passenger = new Passenger();
        passenger.setFirstName("Alice");
        passenger.setLastName("Smith");
        passenger.setEmail("alice@example.com");
        passenger.setPhoneNumber("+123456789");
        passengerRepository.save(passenger);

        Passenger found = passengerRepository.findByEmail("alice@example.com");

        Assertions.assertNotNull(found);
        Assertions.assertEquals("Alice", found.getFirstName());
    }

    @Test
    void testFindAllByStatus() {
        Trip trip1 = new Trip();
        trip1.setStatus("COMPLETED");
        trip1.setCost(15.5);
        tripRepository.save(trip1);

        Trip trip2 = new Trip();
        trip2.setStatus("ACTIVE");
        trip2.setCost(5.0);
        tripRepository.save(trip2);

        Trip trip3 = new Trip();
        trip3.setStatus("COMPLETED");
        trip3.setCost(20.0);
        tripRepository.save(trip3);

        List<Trip> completedTrips = tripRepository.findAllByStatus("COMPLETED");

        Assertions.assertEquals(2, completedTrips.size());
    }
}