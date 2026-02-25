package org.example.sbdbaspectscourse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "vehicles")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "vehicle_type")
@NamedQueries({
        @NamedQuery(name = "Vehicle.deleteByStatus", query = "DELETE FROM Vehicle v WHERE v.status = :status"),
        @NamedQuery(name = "Vehicle.updateStatus", query = "UPDATE Vehicle v SET v.status = :newStatus WHERE v.status = :oldStatus")
})
public abstract class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String status;

    @OneToOne(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private VehicleFinalStatistic vehicleFinalStatistic;
}
