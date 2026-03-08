package org.example.sbdbaspectscourse.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveTrip {
    private String id;
    private String driverId;
    private String passengerId;
    private String status;
}