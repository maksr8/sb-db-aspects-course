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
@NamedQueries({
        @NamedQuery(
                name = "Car.findCapacityGreaterThanAverage",
                query = "SELECT c FROM Car c WHERE c.trunkCapacity > (SELECT AVG(c2.trunkCapacity) FROM Car c2)"
        ),
        @NamedQuery(
                name = "Car.calculateTotalCapacityByStatus",
                query = "SELECT SUM(c.trunkCapacity) FROM Car c WHERE c.status = :status"
        ),
        @NamedQuery(
                name = "Car.findVehicleDetailsNamed",
                query = """
                            SELECT new org.example.sbdbaspectscourse.dto.CarDetailsDto(
                                c.licensePlate,
                                c.model,
                                s.averageRating,
                                s.totalFeedbacks
                            )
                            FROM Car c
                            JOIN c.vehicleFinalStatistic s
                            WHERE c.status = :status
                        """
        )
})
public class Car extends Vehicle {

    @Column(nullable = false)
    private String model;

    @Column(name = "trunk_capacity", nullable = false)
    private Double trunkCapacity;
}
