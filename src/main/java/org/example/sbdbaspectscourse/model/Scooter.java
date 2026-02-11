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

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "max_speed")
    private Integer maxSpeed;
}
