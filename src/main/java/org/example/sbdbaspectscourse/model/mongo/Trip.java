package org.example.sbdbaspectscourse.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "trips")
public class Trip {
    @Id
    private String id;
    private String passengerId;
    private String driverId;
    private String vehiclePlate;
    private Double cost;
    private String status;
}