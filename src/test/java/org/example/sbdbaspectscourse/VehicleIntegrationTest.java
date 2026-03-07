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
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@DataJpaTest
@Testcontainers
@Import(VehicleJdbcDao.class)
class VehicleIntegrationTest extends AbstractTestcontainersSetupTest {
    @Autowired
    private VehicleJdbcDao vehicleJdbcDao;
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Test
    void testUniqueLicensePlateConstraint() {
        Car firstCar = new Car();
        firstCar.setLicensePlate("UNIQUE-123");
        firstCar.setStatus("AVAILABLE");
        firstCar.setModel("Test Model");
        firstCar.setTrunkCapacity(500.0);

        vehicleRepository.saveAndFlush(firstCar);

        Car secondCar = new Car();
        secondCar.setLicensePlate("UNIQUE-123");
        secondCar.setStatus("BUSY");
        secondCar.setModel("Another Model");
        secondCar.setTrunkCapacity(300.0);

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            vehicleRepository.saveAndFlush(secondCar);
        });
    }

    @Test
    void testUniqueLicensePlateIndexExists() {
        Integer indexCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pg_indexes WHERE tablename = 'vehicles' AND indexname = 'uk_vehicles_license_plate'",
                Integer.class
        );

        Assertions.assertNotNull(indexCount);
        Assertions.assertEquals(1, indexCount);
    }

    @Test
    void testTrunkCapacityIndexExists() {
        Integer indexCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pg_indexes WHERE tablename = 'cars' AND indexname = 'idx_cars_trunk_capacity'",
                Integer.class
        );

        Assertions.assertNotNull(indexCount);
        Assertions.assertEquals(1, indexCount);
    }
}
