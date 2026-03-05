package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.exception.VehicleCheckedException;
import org.example.sbdbaspectscourse.exception.VehicleRuntimeException;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.example.sbdbaspectscourse.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.IllegalTransactionStateException;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest
class TransactionDemoTest extends AbstractTestcontainersSetupTest {

    @Autowired
    private TransactionDemoService transactionDemoService;

    @Autowired
    private DeclarativeTransactionService declarativeService;

    @Autowired
    private TemplateTransactionService templateService;

    @Autowired
    private OuterPropagationService outerService;

    @Autowired
    private InnerPropagationService innerService;

    @Autowired
    private EntityManagerTransactionService entityManagerService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void testRollbackOnCheckedException() {
        long initialCount = vehicleRepository.count();

        CarDto carDto = new CarDto();
        carDto.setLicensePlate("CHK-ROLL-01");
        carDto.setStatus("TESTING");
        carDto.setModel("Rollback Test Model");
        carDto.setTrunkCapacity(500.0);

        Assertions.assertThrows(VehicleCheckedException.class, () -> {
            transactionDemoService.saveCarWithCheckedExceptionRollback(carDto);
        });

        long finalCount = vehicleRepository.count();
        Assertions.assertEquals(initialCount, finalCount);

        boolean isVehiclePresent = vehicleRepository.findAll().stream()
                .anyMatch(v -> "CHK-ROLL-01".equals(v.getLicensePlate()));

        Assertions.assertFalse(isVehiclePresent);
    }

    @Test
    void testNoRollbackOnRuntimeException() {
        long initialCount = vehicleRepository.count();

        CarDto carDto = new CarDto();
        carDto.setLicensePlate("RT-NO-ROLL-01");
        carDto.setStatus("TESTING");
        carDto.setModel("No Rollback Test Model");
        carDto.setTrunkCapacity(600.0);

        Assertions.assertThrows(VehicleRuntimeException.class, () -> {
            transactionDemoService.saveCarWithRuntimeExceptionNoRollback(carDto);
        });

        long finalCount = vehicleRepository.count();
        Assertions.assertEquals(initialCount + 1, finalCount);

        boolean isVehiclePresent = vehicleRepository.findAll().stream()
                .anyMatch(v -> "RT-NO-ROLL-01".equals(v.getLicensePlate()));

        Assertions.assertTrue(isVehiclePresent);
    }

    @Test
    void testSelfInvocationDoesNotStartTransaction() {
        boolean isActive = transactionDemoService.checkTransactionWithSelfInvocation();
        Assertions.assertFalse(isActive);
    }

    @Test
    void testExtractedCallStartsTransaction() {
        boolean isActive = transactionDemoService.checkTransactionWithExtractedService();
        Assertions.assertTrue(isActive);
    }

    @Test
    void testSerializableConflict() throws InterruptedException {
        Car car = new Car();
        car.setLicensePlate("SER-CONF-01");
        car.setStatus("FREE");
        car.setModel("Conflict Model");
        car.setTrunkCapacity(100.0);
        car = vehicleRepository.save(car);

        Long id = car.getId();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicReference<Exception> thread1Exception = new AtomicReference<>();
        AtomicReference<Exception> thread2Exception = new AtomicReference<>();

        Thread t1 = new Thread(() -> {
            try {
                latch.await();
                transactionDemoService.updateVehicleCapacitySerializable(id, 50.0);
            } catch (Exception e) {
                thread1Exception.set(e);
            } finally {
                doneLatch.countDown();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                latch.await();
                transactionDemoService.updateVehicleCapacitySerializable(id, 70.0);
            } catch (Exception e) {
                thread2Exception.set(e);
            } finally {
                doneLatch.countDown();
            }
        });

        t1.start();
        t2.start();

        latch.countDown();

        doneLatch.await();

        boolean conflictOccurred = isSerializationFailure(thread1Exception.get()) ||
                isSerializationFailure(thread2Exception.get());

        Assertions.assertTrue(conflictOccurred);
    }

    private boolean isSerializationFailure(Throwable ex) {
        while (ex != null) {
            if (ex instanceof ConcurrencyFailureException) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }

    @Test
    void testDeclarativeTransactions() {
        CarDto dto = new CarDto();
        dto.setLicensePlate("DECL-01");
        dto.setStatus("DECLARATIVE");
        dto.setModel("Transaction Test Model");
        dto.setTrunkCapacity(450.0);

        Car car = declarativeService.createVehicle(dto);
        Assertions.assertNotNull(car.getId());

        declarativeService.updateVehicleStatus(car.getId(), "DECL-UPDATED");
        Car updatedCar = (Car) vehicleRepository.findById(car.getId()).orElseThrow();
        Assertions.assertEquals("DECL-UPDATED", updatedCar.getStatus());

        declarativeService.deleteVehicle(car.getId());
        Assertions.assertTrue(vehicleRepository.findById(car.getId()).isEmpty());
    }

    @Test
    void testTemplateTransactions() {
        CarDto dto = new CarDto();
        dto.setLicensePlate("TMPL-01");
        dto.setStatus("TEMPLATE");
        dto.setModel("Transaction Test Model");
        dto.setTrunkCapacity(450.0);

        Car car = templateService.createVehicle(dto);
        Assertions.assertNotNull(car.getId());

        templateService.updateVehicleStatus(car.getId(), "TMPL-UPDATED");
        Car updatedCar = (Car) vehicleRepository.findById(car.getId()).orElseThrow();
        Assertions.assertEquals("TMPL-UPDATED", updatedCar.getStatus());

        templateService.deleteVehicle(car.getId());
        Assertions.assertTrue(vehicleRepository.findById(car.getId()).isEmpty());
    }

    @Test
    void testEntityManagerTransactions() {
        CarDto dto = new CarDto();
        dto.setLicensePlate("EM-01");
        dto.setStatus("MANUAL");
        dto.setModel("Transaction Test Model");
        dto.setTrunkCapacity(450.0);

        Car car = entityManagerService.createVehicle(dto);
        Assertions.assertNotNull(car.getId());

        entityManagerService.updateVehicleStatus(car.getId(), "EM-UPDATED");
        Car updatedCar = (Car) vehicleRepository.findById(car.getId()).orElseThrow();
        Assertions.assertEquals("EM-UPDATED", updatedCar.getStatus());

        entityManagerService.deleteVehicle(car.getId());
        Assertions.assertTrue(vehicleRepository.findById(car.getId()).isEmpty());
    }

    @Test
    void testMandatoryPropagation() {
        CarDto innerDto = new CarDto();
        innerDto.setLicensePlate("MANDATORY-FAIL");
        innerDto.setStatus("FAILED");
        innerDto.setModel("Inner Model");
        innerDto.setTrunkCapacity(200.0);

        Assertions.assertThrows(IllegalTransactionStateException.class, () -> {
            innerService.saveMandatory(innerDto);
        });

        CarDto outerDto = new CarDto();
        outerDto.setLicensePlate("MANDATORY-OUTER");
        outerDto.setStatus("SUCCESS");
        outerDto.setModel("Outer Model");
        outerDto.setTrunkCapacity(100.0);

        CarDto innerSuccessDto = new CarDto();
        innerSuccessDto.setLicensePlate("MANDATORY-INNER");
        innerSuccessDto.setStatus("SUCCESS");
        innerSuccessDto.setModel("Inner Model");
        innerSuccessDto.setTrunkCapacity(200.0);

        outerService.executeWithMandatoryInner(outerDto, innerSuccessDto);

        Assertions.assertTrue(vehicleRepository.findAll().stream()
                .anyMatch(v -> "MANDATORY-OUTER".equals(v.getLicensePlate())));
        Assertions.assertTrue(vehicleRepository.findAll().stream()
                .anyMatch(v -> "MANDATORY-INNER".equals(v.getLicensePlate())));
    }

    @Test
    void testNotSupportedPropagation() {
        CarDto outerDto = new CarDto();
        outerDto.setLicensePlate("OUTER-NOT-SUP");
        outerDto.setStatus("SUCCESS");
        outerDto.setModel("Outer Model");
        outerDto.setTrunkCapacity(100.0);

        boolean isInnerTransactionActive = outerService.verifyNotSupportedSuspendsTransaction(outerDto);

        Assertions.assertFalse(isInnerTransactionActive, "NOT_SUPPORTED should suspend the transaction, so this must be false");

        boolean isOuterPresent = vehicleRepository.findAll().stream()
                .anyMatch(v -> "OUTER-NOT-SUP".equals(v.getLicensePlate()));

        Assertions.assertTrue(isOuterPresent, "Outer transaction should have resumed and successfully committed the car");
    }

    @Test
    void testNestedPropagation() {
        CarDto outerDto = new CarDto();
        outerDto.setLicensePlate("OUTER-NESTED");
        outerDto.setStatus("SUCCESS");
        outerDto.setModel("Outer Model");
        outerDto.setTrunkCapacity(100.0);

        CarDto innerDto = new CarDto();
        innerDto.setLicensePlate("INNER-NESTED");
        innerDto.setStatus("FAILED");
        innerDto.setModel("Inner Model");
        innerDto.setTrunkCapacity(200.0);

        outerService.executeWithNestedInner(outerDto, innerDto);

        boolean isOuterPresent = vehicleRepository.findAll().stream()
                .anyMatch(v -> "OUTER-NESTED".equals(v.getLicensePlate()));
        boolean isInnerPresent = vehicleRepository.findAll().stream()
                .anyMatch(v -> "INNER-NESTED".equals(v.getLicensePlate()));

        Assertions.assertTrue(isOuterPresent);
        Assertions.assertFalse(isInnerPresent);
    }
}