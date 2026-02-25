package org.example.sbdbaspectscourse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDetailsDto {

    @NotBlank(message = "License plate is mandatory")
    private String licensePlate;

    @NotBlank(message = "Model name is required")
    private String model;

    @NotNull(message = "Average rating is required")
    @Min(value = 0, message = "Rating cannot be negative")
    private Double averageRating;

    @NotNull(message = "Total feedbacks count is required")
    @Min(value = 0, message = "Feedbacks count cannot be negative")
    private Integer totalFeedbacks;
}