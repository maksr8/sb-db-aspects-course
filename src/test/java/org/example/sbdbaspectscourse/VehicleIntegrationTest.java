package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.dao.VehicleJdbcDao;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.model.Scooter;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.TimeZone;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(VehicleJdbcDao.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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

    @Test
    @Sql(scripts = "/extra_data.sql")
    void testLoadDataWithSqlAnnotation() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        boolean hasBatmobile = vehicles.stream()
                .filter(v -> v instanceof Car)
                .map(v -> (Car) v)
                .anyMatch(car -> "Batmobile".equals(car.getModel()));

        Assertions.assertTrue(hasBatmobile, "Data should be loaded using @Sql");
    }

    @Test
    void testNativeScooterJoin() {
        List<Scooter> scooters = vehicleRepository.findAllScootersNative();

        Assertions.assertFalse(scooters.isEmpty(), "There should be at least one scooter from native query");

        Scooter targetScooter = scooters.stream()
                .filter(s -> "SCOOT-99".equals(s.getLicensePlate()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Scooter with plate SCOOT-99 should be present"));

        Assertions.assertEquals("FREE", targetScooter.getStatus());

        Assertions.assertEquals(85, targetScooter.getBatteryLevel(), "Battery level should be 85");
        Assertions.assertEquals(25, targetScooter.getMaxSpeed(), "Max speed should be 25");
    }

    @Test
    void testNativeCarJoin() {  
        List<Car> cars = vehicleRepository.findAllCarsNative();

        Assertions.assertFalse(cars.isEmpty(), "There should be at least one car from native query");

        Car targetCar = cars.stream()
                .filter(c -> "AA1234BB".equals(c.getLicensePlate()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Car with plate AA1234BB should be present"));

        Assertions.assertEquals("AVAILABLE", targetCar.getStatus());

        Assertions.assertEquals("Toyota Camry", targetCar.getModel());
        Assertions.assertEquals(500.0, targetCar.getTrunkCapacity(), 0.001);
    }
}
