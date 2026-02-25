package org.example.sbdbaspectscourse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "vehicle_final_stats")
public class VehicleFinalStatistic {

    @Id
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "total_feedbacks", nullable = false)
    private Integer totalFeedbacks;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating;
}
