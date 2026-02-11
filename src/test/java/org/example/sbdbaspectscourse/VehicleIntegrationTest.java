package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.dao.VehicleJdbcDao;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.TimeZone;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class VehicleIntegrationTest {

    static {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));

    @Autowired
    private VehicleJdbcDao vehicleJdbcDao;
    @Autowired
    private VehicleRepository vehicleRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testSqlDataLoadingAndJdbcQuery() {
        List<String> plates = vehicleJdbcDao.findAllLicensePlates();

        System.out.println("Loaded plates: " + plates);

        Assertions.assertTrue(plates.contains("AA1234BB"), "Must be a car plate from data.sql");
        Assertions.assertTrue(plates.contains("SCOOT-99"), "Must be a scooter plate from data.sql");
    }

    @Test
    void testInheritanceAndComplexQuery() {
        List<Car> cars = vehicleJdbcDao.findAllCarsDetailed();

        Assertions.assertEquals(1, cars.size(), "There should be only one car (scooter ignored)");
        Car car = cars.getFirst();
        Assertions.assertEquals("Toyota Camry", car.getModel());
        Assertions.assertEquals(500.0, car.getTrunkCapacity());
    }

    @Test
    void testJpaRepository() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        Assertions.assertEquals(2, vehicles.size(), "There should be 2 vehicles");
    }

    @Test
    void testJdbcUpdateStatus() {
        Long vehicleId = 1L;
        String newStatus = "ON_REPAIR";

        int rowsAffected = vehicleJdbcDao.updateStatus(vehicleId, newStatus);

        Assertions.assertEquals(1, rowsAffected, "Exactly 1 row must be updated");

        Vehicle updatedVehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        Assertions.assertEquals(newStatus, updatedVehicle.getStatus(), "Status in database must change");
    }
}
