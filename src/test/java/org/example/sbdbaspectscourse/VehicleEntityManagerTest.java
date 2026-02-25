package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.dao.VehicleEntityManagerDao;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(VehicleEntityManagerDao.class)
class VehicleEntityManagerTest extends AbstractTestcontainersSetupTest {
    @Autowired
    private VehicleEntityManagerDao emDao;

    private Car createValidCar(String licensePlate) {
        Car car = new Car();
        car.setLicensePlate(licensePlate);
        car.setStatus("AVAILABLE");
        car.setModel("Test Model");
        car.setTrunkCapacity(500.0);
        return car;
    }

    @Test
    void testPersistAndFind() {
        Car newCar = createValidCar("TEST-PERSIST");
        Assertions.assertNull(newCar.getId(), "ID must be null before persist");

        emDao.persistVehicle(newCar);
        Assertions.assertNotNull(newCar.getId(), "ID must be generated after persist");

        emDao.flush();
        emDao.clear();

        Vehicle foundVehicle = emDao.findVehicle(newCar.getId());
        Assertions.assertNotNull(foundVehicle, "Vehicle should be found in DB");
        Assertions.assertEquals("TEST-PERSIST", foundVehicle.getLicensePlate());
    }

    @Test
    void testDetach() {
        Car car = createValidCar("TEST-DETACH");
        emDao.persistVehicle(car);
        emDao.flush();

        emDao.detachVehicle(car);
        car.setStatus("STOLEN");

        emDao.flush();
        emDao.clear();

        Vehicle dbVehicle = emDao.findVehicle(car.getId());
        Assertions.assertEquals("AVAILABLE", dbVehicle.getStatus(), "Detached entity changes should not be saved");
    }

    @Test
    void testMerge() {
        Car car = createValidCar("TEST-MERGE");
        emDao.persistVehicle(car);
        emDao.flush();
        emDao.clear();

        car.setStatus("IN_REPAIR");
        Vehicle mergedCar = emDao.mergeVehicle(car);

        emDao.flush();
        emDao.clear();

        Vehicle dbVehicle = emDao.findVehicle(mergedCar.getId());
        Assertions.assertEquals("IN_REPAIR", dbVehicle.getStatus(), "Status must be updated after merge");
    }

    @Test
    void testRefresh() {
        Car car = createValidCar("TEST-REFRESH");
        emDao.persistVehicle(car);
        emDao.flush();

        car.setStatus("DESTROYED");
        emDao.refreshVehicle(car);

        Assertions.assertEquals("AVAILABLE", car.getStatus(), "Refresh must restore state from DB");
    }

    @Test
    void testRemove() {
        Car car = createValidCar("TEST-REMOVE");
        emDao.persistVehicle(car);
        emDao.flush();
        Long id = car.getId();

        Assertions.assertNotNull(emDao.findVehicle(id), "Vehicle must exist before removal");

        emDao.removeVehicle(car);
        emDao.flush();

        Assertions.assertNull(emDao.findVehicle(id), "Vehicle must be null after removal");
    }
}