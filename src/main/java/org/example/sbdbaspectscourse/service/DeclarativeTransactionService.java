package org.example.sbdbaspectscourse.service;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.mapper.VehicleMapper;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeclarativeTransactionService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public Car createVehicle(CarDto carDto) {
        Car car = vehicleMapper.toEntity(carDto);
        return vehicleRepository.save(car);
    }

    @Transactional
    public void updateVehicleStatus(Long id, String newStatus) {
        vehicleRepository.findById(id).ifPresent(vehicle -> {
            vehicle.setStatus(newStatus);
            vehicleRepository.save(vehicle);
        });
    }

    @Transactional
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}