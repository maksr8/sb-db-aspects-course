package org.example.sbdbaspectscourse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.dto.ScooterDto;
import org.example.sbdbaspectscourse.dto.VehicleDto;
import org.example.sbdbaspectscourse.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping("/car")
    public CarDto createCar(@RequestBody @Valid CarDto dto) {
        return vehicleService.createCar(dto);
    }

    @PostMapping("/scooter")
    public ScooterDto createScooter(@RequestBody @Valid ScooterDto dto) {
        return vehicleService.createScooter(dto);
    }

    @GetMapping
    public List<VehicleDto> getAll() {
        return vehicleService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getById(@PathVariable Long id) {
        Optional<VehicleDto> opt = vehicleService.getById(id);
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/car/{id}")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long id, @RequestBody @Valid CarDto updatedCarDto) {
        Optional<CarDto> opt = vehicleService.updateCar(id, updatedCarDto);
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/scooter/{id}")
    public ResponseEntity<ScooterDto> updateScooter(@PathVariable Long id, @RequestBody @Valid ScooterDto updatedScooterDto) {
        Optional<ScooterDto> opt = vehicleService.updateScooter(id, updatedScooterDto);
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = vehicleService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/plates")
    public List<String> getPlatesJdbc() {
        return vehicleService.getAllLicensePlates();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatusJdbc(@PathVariable Long id, @RequestParam String status) {
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body("Status cannot be empty");
        }
        boolean updated = vehicleService.updateStatus(id, status);
        return updated ? ResponseEntity.ok("Updated") : ResponseEntity.notFound().build();
    }

    @GetMapping("/cars-jdbc")
    public List<CarDto> getCarsJdbc() {
        return vehicleService.getCarsJdbc();
    }
}
