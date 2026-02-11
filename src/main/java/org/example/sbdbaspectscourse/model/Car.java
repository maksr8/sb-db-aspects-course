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

    private String model;

    @Column(name = "trunk_capacity")
    private Double trunkCapacity;
}
