package org.example.sbdbaspectscourse.service;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.mapper.VehicleMapper;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class InnerPropagationService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveMandatory(CarDto dto) {
        vehicleRepository.save(vehicleMapper.toEntity(dto));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean checkTransactionActiveInNotSupported() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Transactional(propagation = Propagation.NESTED)
    public void saveNestedAndThrow(CarDto dto) {
        Car car = vehicleRepository.save(vehicleMapper.toEntity(dto));
        throw new RuntimeException("Inner exception triggered in NESTED");
    }
}