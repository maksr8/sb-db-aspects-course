package org.example.sbdbaspectscourse.service;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.exception.VehicleCheckedException;
import org.example.sbdbaspectscourse.exception.VehicleRuntimeException;
import org.example.sbdbaspectscourse.mapper.VehicleMapper;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class TransactionDemoService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final ExtractedTransactionService extractedTransactionService;

    @Transactional(rollbackFor = VehicleCheckedException.class)
    public void saveCarWithCheckedExceptionRollback(CarDto carDto) throws VehicleCheckedException {
        Car car = vehicleMapper.toEntity(carDto);
        vehicleRepository.save(car);

        throw new VehicleCheckedException("Simulated checked exception to trigger rollback");
    }

    @Transactional(noRollbackFor = VehicleRuntimeException.class)
    public void saveCarWithRuntimeExceptionNoRollback(CarDto carDto) {
        Car car = vehicleMapper.toEntity(carDto);
        vehicleRepository.save(car);

        throw new VehicleRuntimeException("Simulated runtime exception to test no-rollback");
    }

    @Transactional
    public boolean innerTransactionalMethod() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    public boolean checkTransactionWithSelfInvocation() {
        return this.innerTransactionalMethod();
    }

    public boolean checkTransactionWithExtractedService() {
        return extractedTransactionService.checkTransactionActive();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateVehicleCapacitySerializable(Long vehicleId, Double additionalCapacity) {
        Car car = (Car) vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        car.setTrunkCapacity(car.getTrunkCapacity() + additionalCapacity);
        vehicleRepository.save(car);
    }
}