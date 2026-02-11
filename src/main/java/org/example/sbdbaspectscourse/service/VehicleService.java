package org.example.sbdbaspectscourse.service;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dao.VehicleJdbcDao;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.dto.ScooterDto;
import org.example.sbdbaspectscourse.dto.VehicleDto;
import org.example.sbdbaspectscourse.mapper.VehicleMapper;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.model.Scooter;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.example.sbdbaspectscourse.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper mapper;
    private final VehicleJdbcDao vehicleJdbcDao;

    @Transactional
    public CarDto createCar(CarDto dto) {
        Car entity = mapper.toEntity(dto);
        return mapper.toDto(vehicleRepository.save(entity));
    }

    @Transactional
    public ScooterDto createScooter(ScooterDto dto) {
        Scooter entity = mapper.toEntity(dto);
        return mapper.toDto(vehicleRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getAll() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<VehicleDto> getById(Long id) {
        return vehicleRepository.findById(id)
                .map(this::mapToDto);
    }

    @Transactional
    public Optional<CarDto> updateCar(Long id, CarDto dto) {
        return vehicleRepository.findById(id)
                .filter(v -> v instanceof Car)
                .map(v -> (Car) v)
                .map(car -> {
                    car.setLicensePlate(dto.getLicensePlate());
                    car.setStatus(dto.getStatus());
                    car.setModel(dto.getModel());
                    car.setTrunkCapacity(dto.getTrunkCapacity());
                    return vehicleRepository.save(car);
                })
                .map(mapper::toDto);
    }

    @Transactional
    public Optional<ScooterDto> updateScooter(Long id, ScooterDto dto) {
        return vehicleRepository.findById(id)
                .filter(v -> v instanceof Scooter)
                .map(v -> (Scooter) v)
                .map(scooter -> {
                    scooter.setLicensePlate(dto.getLicensePlate());
                    scooter.setStatus(dto.getStatus());
                    scooter.setBatteryLevel(dto.getBatteryLevel());
                    scooter.setMaxSpeed(dto.getMaxSpeed());
                    return vehicleRepository.save(scooter);
                })
                .map(mapper::toDto);
    }

    @Transactional
    public boolean delete(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<CarDto> getCarsJdbc() {
        return vehicleJdbcDao.findAllCarsDetailed().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllLicensePlates() {
        return vehicleJdbcDao.findAllLicensePlates();
    }

    @Transactional
    public boolean updateStatus(Long id, String status) {
        return vehicleJdbcDao.updateStatus(id, status) > 0;
    }

    private VehicleDto mapToDto(Vehicle v) {
        if (v instanceof Car car) return mapper.toDto(car);
        if (v instanceof Scooter scooter) return mapper.toDto(scooter);
        return null;
    }
}
