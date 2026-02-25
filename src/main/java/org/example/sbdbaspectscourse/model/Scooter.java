package org.example.sbdbaspectscourse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "scooters")
@PrimaryKeyJoinColumn(name = "vehicle_id")
@DiscriminatorValue("SCOOTER")
public class Scooter extends Vehicle {

    @Column(name = "battery_level", nullable = false)
    private Integer batteryLevel;

    @Column(name = "max_speed", nullable = false)
    private Integer maxSpeed;
}
