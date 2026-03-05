package org.example.sbdbaspectscourse.service;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.mapper.VehicleMapper;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class OuterPropagationService {

    private final InnerPropagationService innerPropagationService;
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public void executeWithMandatoryInner(CarDto outerDto, CarDto innerDto) {
        vehicleRepository.save(vehicleMapper.toEntity(outerDto));
        innerPropagationService.saveMandatory(innerDto);
    }

    @Transactional
    public boolean verifyNotSupportedSuspendsTransaction(CarDto outerDto) {
        boolean isOuterActive = TransactionSynchronizationManager.isActualTransactionActive();
        System.out.println("Outer transaction active: " + isOuterActive);

        boolean isInnerActive = innerPropagationService.checkTransactionActiveInNotSupported();
        System.out.println("Inner transaction active (NOT_SUPPORTED): " + isInnerActive);

        vehicleRepository.save(vehicleMapper.toEntity(outerDto));

        return isInnerActive;
    }

    @Transactional
    public void executeWithNestedInner(CarDto outerDto, CarDto innerDto) {
        vehicleRepository.save(vehicleMapper.toEntity(outerDto));

        try {
            innerPropagationService.saveNestedAndThrow(innerDto);
        } catch (RuntimeException e) {
            System.out.println("Outer caught inner NESTED exception: " + e.getMessage());
        }
    }
}