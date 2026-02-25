package org.example.sbdbaspectscourse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "cars")
@PrimaryKeyJoinColumn(name = "vehicle_id")
@DiscriminatorValue("CAR")
public class Car extends Vehicle {

    @Column(nullable = false)
    private String model;

    @Column(name = "trunk_capacity", nullable = false)
    private Double trunkCapacity;
}
