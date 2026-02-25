package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.dao.AdvancedQueryDao;
import org.example.sbdbaspectscourse.dto.CarDetailsDto;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.model.Scooter;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jooq.test.autoconfigure.AutoConfigureJooq;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@DataJpaTest
@AutoConfigureJooq
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AdvancedQueryDao.class)
class AdvancedQueryDaoTest extends AbstractTestcontainersSetupTest {

    @Autowired
    private AdvancedQueryDao advancedQueryDao;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDeleteVehiclesByStatusJpql() {
        Car car = new Car();
        car.setLicensePlate("DEL-123");
        car.setModel("Test Model");
        car.setTrunkCapacity(200.0);
        car.setStatus("DESTROYED");
        vehicleRepository.save(car);

        long countBeforeDeletion = vehicleRepository.count();

        int deletedCount = advancedQueryDao.deleteVehiclesByStatusJpql("DESTROYED");

        Assertions.assertEquals(1, deletedCount, "Exactly one vehicle should be deleted");
        Assertions.assertEquals(countBeforeDeletion - 1, vehicleRepository.count(), "Database count should decrease by 1");
    }

    @Test
    void testDeleteVehiclesByStatusNamedQuery() {
        Scooter scooter = new Scooter();
        scooter.setLicensePlate("SCOOT-DEL-NQ");
        scooter.setBatteryLevel(15);
        scooter.setMaxSpeed(25);
        scooter.setStatus("SOLD");
        vehicleRepository.save(scooter);

        long countBeforeDeletion = vehicleRepository.count();

        int deletedCount = advancedQueryDao.deleteVehiclesByStatusNamedQuery("SOLD");

        Assertions.assertEquals(1, deletedCount, "Exactly one scooter should be deleted via NamedQuery");
        Assertions.assertEquals(countBeforeDeletion - 1, vehicleRepository.count(), "Database count should decrease by 1");
    }

    @Test
    void testDeleteVehiclesByStatusCriteria() {
        Car car = new Car();
        car.setLicensePlate("CRIT-DEL");
        car.setModel("Criteria Model");
        car.setTrunkCapacity(300.0);
        car.setStatus("ARCHIVED");
        vehicleRepository.save(car);

        long countBeforeDeletion = vehicleRepository.count();

        int deletedCount = advancedQueryDao.deleteVehiclesByStatusCriteria("ARCHIVED");

        Assertions.assertEquals(1, deletedCount, "Exactly one car should be deleted via Criteria API");
        Assertions.assertEquals(countBeforeDeletion - 1, vehicleRepository.count(), "Database count should decrease by 1");
    }

    @Test
    void testDeleteVehiclesByStatusNative() {
        Car car = new Car();
        car.setLicensePlate("NAT-DEL");
        car.setModel("Native Model");
        car.setTrunkCapacity(400.0);
        car.setStatus("TOTALED");
        vehicleRepository.save(car);

        long countBeforeDeletion = vehicleRepository.count();

        int deletedCount = advancedQueryDao.deleteVehiclesByStatusNative("TOTALED");

        Assertions.assertEquals(1, deletedCount, "Exactly one car should be deleted via Native Query");
        Assertions.assertEquals(countBeforeDeletion - 1, vehicleRepository.count(), "Database count should decrease by 1");
    }

    @Test
    void testDeleteVehiclesByStatusJooq() {
        Scooter scooter = new Scooter();
        scooter.setLicensePlate("JOOQ-DEL");
        scooter.setBatteryLevel(50);
        scooter.setMaxSpeed(30);
        scooter.setStatus("SCRAPPED");
        vehicleRepository.save(scooter);

        long countBeforeDeletion = vehicleRepository.count();

        int deletedCount = advancedQueryDao.deleteVehiclesByStatusJooq("SCRAPPED");

        Assertions.assertEquals(1, deletedCount, "Exactly one scooter should be deleted via jOOQ");
        Assertions.assertEquals(countBeforeDeletion - 1, vehicleRepository.count(), "Database count should decrease by 1");
    }

    @Test
    void testUpdateVehicleStatusJpql() {
        Car car = new Car();
        car.setLicensePlate("UPD-JPQL");
        car.setModel("Update Model");
        car.setTrunkCapacity(350.0);
        car.setStatus("NEEDS_REPAIR");
        vehicleRepository.save(car);

        int updatedCount = advancedQueryDao.updateVehicleStatusByOldStatusJpql("NEEDS_REPAIR", "IN_REPAIR");

        Assertions.assertEquals(1, updatedCount, "Exactly one car should be updated");

        // Clear the persistence context to ensure we fetch the updated entity from the database
        advancedQueryDao.clear();

        Vehicle updatedVehicle = vehicleRepository.findById(car.getId()).orElseThrow();
        Assertions.assertEquals("IN_REPAIR", updatedVehicle.getStatus(), "Status should be updated in the database");
    }

    @Test
    void testUpdateVehicleStatusNamedQuery() {
        Scooter scooter = new Scooter();
        scooter.setLicensePlate("UPD-NQ");
        scooter.setBatteryLevel(80);
        scooter.setMaxSpeed(25);
        scooter.setStatus("DISCHARGED");
        vehicleRepository.save(scooter);

        int updatedCount = advancedQueryDao.updateVehicleStatusByOldStatusNamedQuery("DISCHARGED", "CHARGING");

        Assertions.assertEquals(1, updatedCount, "Exactly one scooter should be updated");

        advancedQueryDao.clear();

        Vehicle updatedVehicle = vehicleRepository.findById(scooter.getId()).orElseThrow();
        Assertions.assertEquals("CHARGING", updatedVehicle.getStatus(), "Status should be updated in the database");
    }

    @Test
    void testUpdateVehicleStatusCriteria() {
        Car car = new Car();
        car.setLicensePlate("UPD-CRIT");
        car.setModel("Criteria Update Model");
        car.setTrunkCapacity(400.0);
        car.setStatus("PAINTING");
        vehicleRepository.save(car);

        int updatedCount = advancedQueryDao.updateVehicleStatusByOldStatusCriteria("PAINTING", "READY");

        Assertions.assertEquals(1, updatedCount, "Exactly one car should be updated via Criteria API");

        advancedQueryDao.clear();

        Vehicle updatedVehicle = vehicleRepository.findById(car.getId()).orElseThrow();
        Assertions.assertEquals("READY", updatedVehicle.getStatus(), "Status should be updated in the database");
    }

    @Test
    void testUpdateVehicleStatusNative() {
        Scooter scooter = new Scooter();
        scooter.setLicensePlate("UPD-NAT");
        scooter.setBatteryLevel(90);
        scooter.setMaxSpeed(20);
        scooter.setStatus("LOST");
        vehicleRepository.save(scooter);

        int updatedCount = advancedQueryDao.updateVehicleStatusByOldStatusNative("LOST", "FOUND");

        Assertions.assertEquals(1, updatedCount, "Exactly one scooter should be updated via Native Query");

        advancedQueryDao.clear();

        Vehicle updatedVehicle = vehicleRepository.findById(scooter.getId()).orElseThrow();
        Assertions.assertEquals("FOUND", updatedVehicle.getStatus(), "Status should be updated in the database");
    }

    @Test
    void testUpdateVehicleStatusJooq() {
        Car car = new Car();
        car.setLicensePlate("UPD-JOOQ");
        car.setModel("jOOQ Model");
        car.setTrunkCapacity(500.0);
        car.setStatus("BROKEN");
        vehicleRepository.save(car);

        int updatedCount = advancedQueryDao.updateVehicleStatusByOldStatusJooq("BROKEN", "FIXED");

        Assertions.assertEquals(1, updatedCount, "Exactly one car should be updated via jOOQ");

        advancedQueryDao.clear();

        Vehicle updatedVehicle = vehicleRepository.findById(car.getId()).orElseThrow();
        Assertions.assertEquals("FIXED", updatedVehicle.getStatus(), "Status should be updated in the database");
    }

    @Test
    void testFindCarsWithTrunkCapacityGreaterThanAverageJpql() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("AVG-1");
        car1.setModel("Model A");
        car1.setTrunkCapacity(100.0);
        car1.setStatus("FREE");

        Car car2 = new Car();
        car2.setLicensePlate("AVG-2");
        car2.setModel("Model B");
        car2.setTrunkCapacity(200.0);
        car2.setStatus("FREE");

        Car car3 = new Car();
        car3.setLicensePlate("AVG-3");
        car3.setModel("Model C");
        car3.setTrunkCapacity(300.0);
        car3.setStatus("FREE");

        vehicleRepository.saveAll(List.of(car1, car2, car3));

        List<Car> result = advancedQueryDao.findCarsWithTrunkCapacityGreaterThanAverageJpql();

        Assertions.assertEquals(1, result.size(), "Should find exactly one car");
        Assertions.assertEquals(300.0, result.getFirst().getTrunkCapacity(), "Should find the car with capacity 300.0");
    }

    @Test
    void testFindCarsWithTrunkCapacityGreaterThanAverageNamedQuery() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("AVG-NQ-1");
        car1.setModel("Model NQ A");
        car1.setTrunkCapacity(150.0);
        car1.setStatus("FREE");

        Car car2 = new Car();
        car2.setLicensePlate("AVG-NQ-2");
        car2.setModel("Model NQ B");
        car2.setTrunkCapacity(250.0);
        car2.setStatus("FREE");

        Car car3 = new Car();
        car3.setLicensePlate("AVG-NQ-3");
        car3.setModel("Model NQ C");
        car3.setTrunkCapacity(400.0);
        car3.setStatus("FREE");

        vehicleRepository.saveAll(List.of(car1, car2, car3));

        List<Car> result = advancedQueryDao.findCarsWithTrunkCapacityGreaterThanAverageNamedQuery();

        Assertions.assertEquals(1, result.size(), "Should find exactly one car");
        Assertions.assertEquals(400.0, result.getFirst().getTrunkCapacity(), "Should find the car with capacity 400.0");
    }

    @Test
    void testFindCarsWithTrunkCapacityGreaterThanAverageCriteria() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("AVG-CR-1");
        car1.setModel("Criteria A");
        car1.setTrunkCapacity(100.0);
        car1.setStatus("MAINTENANCE");

        Car car2 = new Car();
        car2.setLicensePlate("AVG-CR-2");
        car2.setModel("Criteria B");
        car2.setTrunkCapacity(250.0);
        car2.setStatus("MAINTENANCE");

        Car car3 = new Car();
        car3.setLicensePlate("AVG-CR-3");
        car3.setModel("Criteria C");
        car3.setTrunkCapacity(350.0);
        car3.setStatus("MAINTENANCE");

        vehicleRepository.saveAll(List.of(car1, car2, car3));

        List<Car> result = advancedQueryDao.findCarsWithTrunkCapacityGreaterThanAverageCriteria();

        Assertions.assertEquals(2, result.size(), "Should find exactly two cars");

        Assertions.assertTrue(result.stream().anyMatch(c -> c.getTrunkCapacity() == 250.0));
        Assertions.assertTrue(result.stream().anyMatch(c -> c.getTrunkCapacity() == 350.0));
    }

    @Test
    void testFindCarsWithTrunkCapacityGreaterThanAverageNative() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("AVG-NAT-1");
        car1.setModel("Native A");
        car1.setTrunkCapacity(200.0);
        car1.setStatus("FREE");

        Car car2 = new Car();
        car2.setLicensePlate("AVG-NAT-2");
        car2.setModel("Native B");
        car2.setTrunkCapacity(200.0);
        car2.setStatus("FREE");

        Car car3 = new Car();
        car3.setLicensePlate("AVG-NAT-3");
        car3.setModel("Native C");
        car3.setTrunkCapacity(200.0);
        car3.setStatus("FREE");

        vehicleRepository.saveAll(List.of(car1, car2, car3));

        List<Car> result = advancedQueryDao.findCarsWithTrunkCapacityGreaterThanAverageNative();

        Assertions.assertTrue(result.isEmpty(), "Should find 0 cars because none is strictly greater than average");

        Car car4 = new Car();
        car4.setLicensePlate("AVG-NAT-4");
        car4.setModel("Native D");
        car4.setTrunkCapacity(500.0);
        car4.setStatus("FREE");
        vehicleRepository.save(car4);

        List<Car> resultAfterAdding = advancedQueryDao.findCarsWithTrunkCapacityGreaterThanAverageNative();
        Assertions.assertEquals(1, resultAfterAdding.size(), "Should find exactly one car after adding a bigger one");
        Assertions.assertEquals(500.0, resultAfterAdding.getFirst().getTrunkCapacity());
    }

    @Test
    void testFindCarsWithTrunkCapacityGreaterThanAverageJooq() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("AVG-JOOQ-1");
        car1.setModel("jOOQ A");
        car1.setTrunkCapacity(100.0);
        car1.setStatus("WASHING");

        Car car2 = new Car();
        car2.setLicensePlate("AVG-JOOQ-2");
        car2.setModel("jOOQ B");
        car2.setTrunkCapacity(100.0);
        car2.setStatus("WASHING");

        Car car3 = new Car();
        car3.setLicensePlate("AVG-JOOQ-3");
        car3.setModel("jOOQ C");
        car3.setTrunkCapacity(400.0);
        car3.setStatus("WASHING");

        vehicleRepository.saveAll(List.of(car1, car2, car3));

        List<Car> result = advancedQueryDao.findCarsWithTrunkCapacityGreaterThanAverageJooq();

        Assertions.assertEquals(1, result.size(), "Should find exactly one car via jOOQ");
        Assertions.assertEquals(400.0, result.getFirst().getTrunkCapacity(), "Should find the car with capacity 400.0");
    }

    @Test
    void testCalculateTotalTrunkCapacityByStatusJpql() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("RES-1");
        car1.setModel("Model A");
        car1.setTrunkCapacity(300.0);
        car1.setStatus("FREE");

        Car car2 = new Car();
        car2.setLicensePlate("RES-2");
        car2.setModel("Model B");
        car2.setTrunkCapacity(450.0);
        car2.setStatus("FREE");

        Car car3 = new Car();
        car3.setLicensePlate("RES-3");
        car3.setModel("Model C");
        car3.setTrunkCapacity(500.0);
        car3.setStatus("IN_USE");

        vehicleRepository.saveAll(List.of(car1, car2, car3));

        Double totalCapacity = advancedQueryDao.calculateTotalTrunkCapacityByStatusJpql("FREE");

        Double emptyCapacity = advancedQueryDao.calculateTotalTrunkCapacityByStatusJpql("BROKEN");

        Assertions.assertEquals(750.0, totalCapacity, "Total capacity for FREE cars should be 750.0");
        Assertions.assertEquals(0.0, emptyCapacity, "If no cars match, should safely return 0.0");
    }

    @Test
    void testCalculateTotalTrunkCapacityByStatusNamedQuery() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("SUM-NQ-1");
        car1.setModel("Model NQ A");
        car1.setTrunkCapacity(200.0);
        car1.setStatus("RESERVED");

        Car car2 = new Car();
        car2.setLicensePlate("SUM-NQ-2");
        car2.setModel("Model NQ B");
        car2.setTrunkCapacity(300.0);
        car2.setStatus("RESERVED");

        vehicleRepository.saveAll(List.of(car1, car2));

        Double result = advancedQueryDao.calculateTotalTrunkCapacityByStatusNamedQuery("RESERVED");

        Assertions.assertEquals(500.0, result, "Sum of capacities should be 500.0");
    }

    @Test
    void testCalculateTotalTrunkCapacityByStatusCriteria() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("CRIT-SUM-1");
        car1.setModel("Model A");
        car1.setTrunkCapacity(100.5);
        car1.setStatus("FREE");

        Car car2 = new Car();
        car2.setLicensePlate("CRIT-SUM-2");
        car2.setModel("Model B");
        car2.setTrunkCapacity(200.5);
        car2.setStatus("FREE");

        Car car3 = new Car();
        car3.setLicensePlate("CRIT-SUM-3");
        car3.setModel("Model C");
        car3.setTrunkCapacity(1000.0);
        car3.setStatus("BUSY");

        vehicleRepository.saveAll(List.of(car1, car2, car3));

        Double totalFree = advancedQueryDao.calculateTotalTrunkCapacityByStatusCriteria("FREE");

        Assertions.assertEquals(301.0, totalFree, "Sum of FREE cars capacity should be 301.0");
    }

    @Test
    void testCalculateTotalTrunkCapacityByStatusNative() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("NAT-SUM-1");
        car1.setModel("Native Model A");
        car1.setTrunkCapacity(250.0);
        car1.setStatus("SERVICE");

        Car car2 = new Car();
        car2.setLicensePlate("NAT-SUM-2");
        car2.setModel("Native Model B");
        car2.setTrunkCapacity(350.0);
        car2.setStatus("SERVICE");

        vehicleRepository.saveAll(List.of(car1, car2));

        Double total = advancedQueryDao.calculateTotalTrunkCapacityByStatusNative("SERVICE");

        Assertions.assertEquals(600.0, total, "Sum of capacities for SERVICE status should be 600.0");
    }

    @Test
    void testCalculateTotalTrunkCapacityByStatusJooq() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("JOOQ-SUM-1");
        car1.setModel("Model X");
        car1.setTrunkCapacity(123.45);
        car1.setStatus("SOLD");

        Car car2 = new Car();
        car2.setLicensePlate("JOOQ-SUM-2");
        car2.setModel("Model Y");
        car2.setTrunkCapacity(200.55);
        car2.setStatus("SOLD");

        vehicleRepository.saveAll(List.of(car1, car2));

        Double total = advancedQueryDao.calculateTotalTrunkCapacityByStatusJooq("SOLD");

        Assertions.assertEquals(324.0, total, "Sum of SOLD cars capacity should be 324.0");
    }

    @Test
    void testFindCarDetailsJpql() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car = new Car();
        car.setLicensePlate("JOIN-DTO-1");
        car.setModel("Volvo XC90");
        car.setStatus("FREE");
        car.setTrunkCapacity(600.0);
        vehicleRepository.save(car);

        jdbcTemplate.update("""
                INSERT INTO vehicle_final_stats (vehicle_id, average_rating, total_feedbacks)
                VALUES (?, ?, ?)
                """, car.getId(), 4.85, 240);

        List<CarDetailsDto> result = advancedQueryDao.findCarDetailsJpql("FREE");

        Assertions.assertFalse(result.isEmpty());
        CarDetailsDto dto = result.getFirst();
        Assertions.assertEquals("JOIN-DTO-1", dto.getLicensePlate());
        Assertions.assertEquals("Volvo XC90", dto.getModel());
        Assertions.assertEquals(4.85, dto.getAverageRating());
        Assertions.assertEquals(240, dto.getTotalFeedbacks());
    }

    @Test
    void testFindCarDetailsNamedQuery() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car = new Car();
        car.setLicensePlate("NAMED-JOIN-1");
        car.setModel("Mercedes E-Class");
        car.setStatus("OCCUPIED");
        car.setTrunkCapacity(540.0);
        vehicleRepository.save(car);

        jdbcTemplate.update("""
                INSERT INTO vehicle_final_stats (vehicle_id, average_rating, total_feedbacks)
                VALUES (?, ?, ?)
                """, car.getId(), 4.7, 88);

        List<CarDetailsDto> result = advancedQueryDao.findCarDetailsNamedQuery("OCCUPIED");

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(4.7, result.getFirst().getAverageRating());
        Assertions.assertEquals("Mercedes E-Class", result.getFirst().getModel());
    }

    @Test
    void testFindCarDetailsCriteria() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car = new Car();
        car.setLicensePlate("CRIT-JOIN-1");
        car.setModel("Porsche Taycan");
        car.setStatus("FREE");
        car.setTrunkCapacity(400.0);
        vehicleRepository.save(car);

        jdbcTemplate.update("""
                INSERT INTO vehicle_final_stats (vehicle_id, average_rating, total_feedbacks)
                VALUES (?, ?, ?)
                """, car.getId(), 4.99, 500);

        List<CarDetailsDto> result = advancedQueryDao.findCarDetailsCriteria("FREE");

        Assertions.assertFalse(result.isEmpty());
        CarDetailsDto dto = result.getFirst();
        Assertions.assertEquals("Porsche Taycan", dto.getModel());
        Assertions.assertEquals(4.99, dto.getAverageRating());
    }

    @Test
    void testFindCarDetailsNative() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car = new Car();
        car.setLicensePlate("NATIVE-JOIN-1");
        car.setModel("Audi RS7");
        car.setStatus("BUSY");
        car.setTrunkCapacity(530.0);
        vehicleRepository.save(car);

        jdbcTemplate.update("""
                INSERT INTO vehicle_final_stats (vehicle_id, average_rating, total_feedbacks)
                VALUES (?, ?, ?)
                """, car.getId(), 4.95, 320);

        List<CarDetailsDto> result = advancedQueryDao.findCarDetailsNative("BUSY");

        Assertions.assertFalse(result.isEmpty());
        CarDetailsDto dto = result.getFirst();
        Assertions.assertEquals("NATIVE-JOIN-1", dto.getLicensePlate());
        Assertions.assertEquals(4.95, dto.getAverageRating());
        Assertions.assertEquals(320, dto.getTotalFeedbacks());
    }

    @Test
    void testFindCarDetailsJooq() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car = new Car();
        car.setLicensePlate("JOOQ-JOIN-1");
        car.setModel("Lexus ES");
        car.setStatus("AVAILABLE");
        car.setTrunkCapacity(480.0);
        vehicleRepository.save(car);

        jdbcTemplate.update("""
                INSERT INTO vehicle_final_stats (vehicle_id, average_rating, total_feedbacks)
                VALUES (?, ?, ?)
                """, car.getId(), 4.92, 110);

        List<CarDetailsDto> result = advancedQueryDao.findCarDetailsJooq("AVAILABLE");

        Assertions.assertFalse(result.isEmpty(), "jOOQ should find the car with its stats");
        CarDetailsDto dto = result.getFirst();
        Assertions.assertEquals("Lexus ES", dto.getModel());
        Assertions.assertEquals(4.92, dto.getAverageRating());
        Assertions.assertEquals(110, dto.getTotalFeedbacks());
    }

    @Test
    void testFindCarsDynamicJpql() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("DYN-JPQL-1");
        car1.setModel("Tesla Model S");
        car1.setStatus("FREE");
        car1.setTrunkCapacity(500.0);
        vehicleRepository.save(car1);

        Car car2 = new Car();
        car2.setLicensePlate("DYN-JPQL-2");
        car2.setModel("Tesla Model X");
        car2.setStatus("BUSY");
        car2.setTrunkCapacity(600.0);
        vehicleRepository.save(car2);

        Car car3 = new Car();
        car3.setLicensePlate("DYN-JPQL-3");
        car3.setModel("BMW M3");
        car3.setStatus("FREE");
        car3.setTrunkCapacity(400.0);
        vehicleRepository.save(car3);

        List<Car> onlyFree = advancedQueryDao.findCarsDynamicJpql("FREE", null, null);
        Assertions.assertEquals(2, onlyFree.size(), "Should find 2 free cars");

        List<Car> teslaBigTrunk = advancedQueryDao.findCarsDynamicJpql(null, "Tesla", 550.0);
        Assertions.assertEquals(1, teslaBigTrunk.size());
        Assertions.assertEquals("DYN-JPQL-2", teslaBigTrunk.getFirst().getLicensePlate());

        List<Car> allCars = advancedQueryDao.findCarsDynamicJpql(null, null, null);
        Assertions.assertEquals(3, allCars.size());
    }

    @Test
    void testFindCarsDynamicCriteria() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("DYN-CR-1");
        car1.setModel("Volvo XC90");
        car1.setStatus("ACTIVE");
        car1.setTrunkCapacity(600.0);
        vehicleRepository.save(car1);

        Car car2 = new Car();
        car2.setLicensePlate("DYN-CR-2");
        car2.setModel("Volvo V60");
        car2.setStatus("FREE");
        car2.setTrunkCapacity(500.0);
        vehicleRepository.save(car2);

        Car car3 = new Car();
        car3.setLicensePlate("DYN-CR-3");
        car3.setModel("Fiat 500");
        car3.setStatus("FREE");
        car3.setTrunkCapacity(180.0);
        vehicleRepository.save(car3);

        List<Car> volvos = advancedQueryDao.findCarsDynamicCriteria("FREE", "volvo", null);
        Assertions.assertEquals(1, volvos.size());
        Assertions.assertEquals("DYN-CR-2", volvos.getFirst().getLicensePlate());

        List<Car> bigTrunks = advancedQueryDao.findCarsDynamicCriteria(null, null, 450.0);
        Assertions.assertEquals(2, bigTrunks.size());

        List<Car> all = advancedQueryDao.findCarsDynamicCriteria(null, null, null);
        Assertions.assertEquals(3, all.size());
    }

    @Test
    void testFindCarsDynamicNative() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("DYN-NAT-1");
        car1.setModel("Renault Megane");
        car1.setStatus("FREE");
        car1.setTrunkCapacity(400.0);
        vehicleRepository.save(car1);

        Car car2 = new Car();
        car2.setLicensePlate("DYN-NAT-2");
        car2.setModel("Renault Zoe");
        car2.setStatus("BUSY");
        car2.setTrunkCapacity(330.0);
        vehicleRepository.save(car2);

        List<Car> renaults = advancedQueryDao.findCarsDynamicNative(null, "renault", null);
        Assertions.assertEquals(2, renaults.size());

        List<Car> busyZoe = advancedQueryDao.findCarsDynamicNative("BUSY", "Zoe", null);
        Assertions.assertEquals(1, busyZoe.size());
        Assertions.assertEquals("DYN-NAT-2", busyZoe.getFirst().getLicensePlate());
    }

    @Test
    void testFindCarsDynamicComplexJpql() {
        jdbcTemplate.execute("DELETE FROM vehicle_final_stats");
        vehicleRepository.deleteAll();
        advancedQueryDao.flush();

        Car car1 = new Car();
        car1.setLicensePlate("DYN-JOOQ-1");
        car1.setModel("Mazda 6");
        car1.setStatus("FREE");
        car1.setTrunkCapacity(480.0);
        vehicleRepository.save(car1);

        Car car2 = new Car();
        car2.setLicensePlate("DYN-JOOQ-2");
        car2.setModel("Mazda CX-5");
        car2.setStatus("BUSY");
        car2.setTrunkCapacity(500.0);
        vehicleRepository.save(car2);

        Car car3 = new Car();
        car3.setLicensePlate("DYN-JOOQ-3");
        car3.setModel("Honda Civic");
        car3.setStatus("FREE");
        car3.setTrunkCapacity(420.0);
        vehicleRepository.save(car3);

        List<Car> largeMazdas = advancedQueryDao.findCarsDynamicJooq(null, "Mazda", 490.0);
        Assertions.assertEquals(1, largeMazdas.size());
        Assertions.assertEquals("DYN-JOOQ-2", largeMazdas.getFirst().getLicensePlate());

        List<Car> freeCars = advancedQueryDao.findCarsDynamicJooq("FREE", null, null);
        Assertions.assertEquals(2, freeCars.size());

        List<Car> all = advancedQueryDao.findCarsDynamicJooq(null, null, null);
        Assertions.assertEquals(3, all.size());
    }
}

