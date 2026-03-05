package org.example.sbdbaspectscourse.service;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.mapper.VehicleMapper;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class TemplateTransactionService {

    private final TransactionTemplate transactionTemplate;
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    public Car createVehicle(CarDto carDto) {
        return transactionTemplate.execute(status -> {
            Car car = vehicleMapper.toEntity(carDto);
            return vehicleRepository.save(car);
        });
    }

    public void updateVehicleStatus(Long id, String newStatus) {
        transactionTemplate.executeWithoutResult(status -> {
            vehicleRepository.findById(id).ifPresent(vehicle -> {
                vehicle.setStatus(newStatus);
                vehicleRepository.save(vehicle);
            });
        });
    }

    public void deleteVehicle(Long id) {
        transactionTemplate.executeWithoutResult(status -> vehicleRepository.deleteById(id));
    }
}