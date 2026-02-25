package org.example.sbdbaspectscourse.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class VehicleEntityManagerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void persistVehicle(Vehicle vehicle) {
        entityManager.persist(vehicle);
    }

    public Vehicle findVehicle(Long id) {
        return entityManager.find(Vehicle.class, id);
    }

    public Vehicle mergeVehicle(Vehicle vehicle) {
        return entityManager.merge(vehicle);
    }

    public void removeVehicle(Vehicle vehicle) {
        Vehicle managedVehicle = entityManager.contains(vehicle) ? vehicle : entityManager.merge(vehicle);
        entityManager.remove(managedVehicle);
    }

    public void detachVehicle(Vehicle vehicle) {
        entityManager.detach(vehicle);
    }

    public void refreshVehicle(Vehicle vehicle) {
        entityManager.refresh(vehicle);
    }

    public void flush() {
        entityManager.flush();
    }

    public void clear() {
        entityManager.clear();
    }
}
