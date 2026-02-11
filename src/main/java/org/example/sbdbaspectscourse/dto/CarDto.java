package org.example.sbdbaspectscourse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarDto implements VehicleDto {
    private Long id;

    @NotBlank(message = "License plate is mandatory")
    private String licensePlate;

    @NotBlank(message = "Status is mandatory")
    private String status;

    @NotBlank(message = "Model name is required")
    private String model;

    @NotNull(message = "Trunk capacity is required")
    @Min(value = 0, message = "Trunk capacity must be a positive number")
    private Double trunkCapacity;
}
