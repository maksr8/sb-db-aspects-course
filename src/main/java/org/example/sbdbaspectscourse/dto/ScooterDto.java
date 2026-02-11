package org.example.sbdbaspectscourse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScooterDto implements VehicleDto {
    private Long id;

    @NotBlank(message = "License plate is mandatory")
    private String licensePlate;

    @NotBlank(message = "Status is mandatory")
    private String status;

    @NotNull(message = "Battery level is required")
    @Min(value = 0, message = "Battery level cannot be less than 0")
    @Max(value = 100, message = "Battery level cannot be more than 100")
    private Integer batteryLevel;

    @NotNull(message = "Max speed is required")
    @Min(value = 1, message = "Max speed must be greater than 0")
    private Integer maxSpeed;
}
