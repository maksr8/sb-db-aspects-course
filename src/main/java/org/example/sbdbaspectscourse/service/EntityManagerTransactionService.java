package org.example.sbdbaspectscourse.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDto;
import org.example.sbdbaspectscourse.mapper.VehicleMapper;
import org.example.sbdbaspectscourse.model.Car;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityManagerTransactionService {

    private final EntityManagerFactory entityManagerFactory;
    private final VehicleMapper vehicleMapper;

    public Car createVehicle(CarDto carDto) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Car car = vehicleMapper.toEntity(carDto);
                em.persist(car);
                transaction.commit();
                return car;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void updateVehicleStatus(Long id, String newStatus) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Car car = em.find(Car.class, id);
                if (car != null) {
                    car.setStatus(newStatus);
                    em.merge(car);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void deleteVehicle(Long id) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Car car = em.find(Car.class, id);
                if (car != null) {
                    em.remove(car);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }
}