package org.example.sbdbaspectscourse.mapper;

import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.dto.ScooterDto;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.model.Scooter;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public CarDto toDto(Car car) {
        if (car == null) return null;
        CarDto dto = new CarDto();
        dto.setId(car.getId());
        dto.setLicensePlate(car.getLicensePlate());
        dto.setStatus(car.getStatus());
        dto.setModel(car.getModel());
        dto.setTrunkCapacity(car.getTrunkCapacity());
        return dto;
    }

    public Car toEntity(CarDto dto) {
        if (dto == null) return null;
        Car car = new Car();
        car.setLicensePlate(dto.getLicensePlate());
        car.setStatus(dto.getStatus());
        car.setModel(dto.getModel());
        car.setTrunkCapacity(dto.getTrunkCapacity());
        return car;
    }

    public ScooterDto toDto(Scooter scooter) {
        if (scooter == null) return null;
        ScooterDto dto = new ScooterDto();
        dto.setId(scooter.getId());
        dto.setLicensePlate(scooter.getLicensePlate());
        dto.setStatus(scooter.getStatus());
        dto.setBatteryLevel(scooter.getBatteryLevel());
        dto.setMaxSpeed(scooter.getMaxSpeed());
        return dto;
    }

    public Scooter toEntity(ScooterDto dto) {
        if (dto == null) return null;
        Scooter scooter = new Scooter();
        scooter.setLicensePlate(dto.getLicensePlate());
        scooter.setStatus(dto.getStatus());
        scooter.setBatteryLevel(dto.getBatteryLevel());
        scooter.setMaxSpeed(dto.getMaxSpeed());
        return scooter;
    }
}

