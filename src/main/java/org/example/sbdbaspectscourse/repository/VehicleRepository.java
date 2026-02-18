package org.example.sbdbaspectscourse.repository;

import org.example.sbdbaspectscourse.model.Scooter;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.example.sbdbaspectscourse.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query(value = """
        SELECT *
        FROM vehicles v
        INNER JOIN scooters s ON v.id = s.vehicle_id
        """, nativeQuery = true)
    List<Scooter> findAllScootersNative();

    @Query(value = """
        SELECT *
        FROM vehicles v
        INNER JOIN cars c ON v.id = c.vehicle_id
        """, nativeQuery = true)
    List<Car> findAllCarsNative();
}
