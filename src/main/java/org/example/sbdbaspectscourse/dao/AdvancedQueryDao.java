package org.example.sbdbaspectscourse.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.dto.CarDetailsDto;
import org.example.sbdbaspectscourse.model.Car;
import org.example.sbdbaspectscourse.model.Vehicle;
import org.example.sbdbaspectscourse.model.VehicleFinalStatistic;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.example.sbdbaspectscourse.jooq.Tables.*;
import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.sum;

@Component
@Transactional
@RequiredArgsConstructor
public class AdvancedQueryDao {

    @PersistenceContext
    private EntityManager entityManager;

    private final DSLContext dsl;

    public void flush() {
        entityManager.flush();
    }

    public void clear() {
        entityManager.clear();
    }

    /**
     * 1. a) Deletion by condition using JPQL.
     * Deletes vehicles matching the specified status.
     */
    public int deleteVehiclesByStatusJpql(String status) {
        String jpql = "DELETE FROM Vehicle v WHERE v.status = :status";

        return entityManager.createQuery(jpql)
                .setParameter("status", status)
                .executeUpdate();
    }

    /**
     * 1. b) Deletion by condition using NamedQuery.
     * Deletes vehicles matching the specified status.
     */
    public int deleteVehiclesByStatusNamedQuery(String status) {
        return entityManager.createNamedQuery("Vehicle.deleteByStatus")
                .setParameter("status", status)
                .executeUpdate();
    }

    /**
     * 1. c) Deletion by condition using Criteria API.
     * Deletes vehicles matching the specified status.
     */
    public int deleteVehiclesByStatusCriteria(String status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaDelete<Vehicle> deleteQuery = cb.createCriteriaDelete(Vehicle.class);

        Root<Vehicle> root = deleteQuery.from(Vehicle.class);

        deleteQuery.where(cb.equal(root.get("status"), status));

        return entityManager.createQuery(deleteQuery).executeUpdate();
    }

    /**
     * 1. d) Deletion by condition using Native Query.
     */
    public int deleteVehiclesByStatusNative(String status) {
        String deleteCars = "DELETE FROM cars WHERE vehicle_id IN (SELECT id FROM vehicles WHERE status = :status)";
        entityManager.createNativeQuery(deleteCars)
                .setParameter("status", status)
                .executeUpdate();

        String deleteScooters = "DELETE FROM scooters WHERE vehicle_id IN (SELECT id FROM vehicles WHERE status = :status)";
        entityManager.createNativeQuery(deleteScooters)
                .setParameter("status", status)
                .executeUpdate();

        String deleteVehicles = "DELETE FROM vehicles WHERE status = :status";
        return entityManager.createNativeQuery(deleteVehicles)
                .setParameter("status", status)
                .executeUpdate();
    }

    /**
     * 1. e) Deletion by condition using JOOQ.
     */
    public int deleteVehiclesByStatusJooq(String status) {
        dsl.deleteFrom(CARS)
                .where(CARS.VEHICLE_ID.in(dsl.select(VEHICLES.ID).from(VEHICLES).where(VEHICLES.STATUS.eq(status))))
                .execute();

        dsl.deleteFrom(SCOOTERS)
                .where(SCOOTERS.VEHICLE_ID.in(dsl.select(VEHICLES.ID).from(VEHICLES).where(VEHICLES.STATUS.eq(status))))
                .execute();

        return dsl.deleteFrom(VEHICLES)
                .where(VEHICLES.STATUS.eq(status))
                .execute();
    }

    /**
     * 2. a) Update by condition using JPQL.
     * Updates the status of vehicles from an old status to a new status.
     */
    public int updateVehicleStatusByOldStatusJpql(String oldStatus, String newStatus) {
        String jpql = "UPDATE Vehicle v SET v.status = :newStatus WHERE v.status = :oldStatus";

        return entityManager.createQuery(jpql)
                .setParameter("newStatus", newStatus)
                .setParameter("oldStatus", oldStatus)
                .executeUpdate();
    }

    /**
     * 2. b) Update by condition using NamedQuery.
     */
    public int updateVehicleStatusByOldStatusNamedQuery(String oldStatus, String newStatus) {
        return entityManager.createNamedQuery("Vehicle.updateStatus")
                .setParameter("newStatus", newStatus)
                .setParameter("oldStatus", oldStatus)
                .executeUpdate();
    }

    /**
     * 2. c) Update by condition using Criteria API.
     */
    public int updateVehicleStatusByOldStatusCriteria(String oldStatus, String newStatus) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaUpdate<Vehicle> updateQuery = cb.createCriteriaUpdate(Vehicle.class);

        Root<Vehicle> root = updateQuery.from(Vehicle.class);

        updateQuery.set(root.get("status"), newStatus);
        updateQuery.where(cb.equal(root.get("status"), oldStatus));

        return entityManager.createQuery(updateQuery).executeUpdate();
    }

    /**
     * 2. d) Update by condition using Native Query.
     */
    public int updateVehicleStatusByOldStatusNative(String oldStatus, String newStatus) {
        String sql = "UPDATE vehicles SET status = :newStatus WHERE status = :oldStatus";

        return entityManager.createNativeQuery(sql)
                .setParameter("newStatus", newStatus)
                .setParameter("oldStatus", oldStatus)
                .executeUpdate();
    }

    /**
     * 2. e) Update by condition using JOOQ.
     */
    public int updateVehicleStatusByOldStatusJooq(String oldStatus, String newStatus) {
        return dsl.update(VEHICLES)
                .set(VEHICLES.STATUS, newStatus)
                .where(VEHICLES.STATUS.eq(oldStatus))
                .execute();
    }

    /**
     * 3. a) Use of aggregation functions in condition using JPQL.
     * Finds cars whose trunk capacity is strictly greater than the average trunk capacity of all cars.
     */
    public List<Car> findCarsWithTrunkCapacityGreaterThanAverageJpql() {
        String jpql = "SELECT c FROM Car c WHERE c.trunkCapacity > (SELECT AVG(c2.trunkCapacity) FROM Car c2)";

        return entityManager.createQuery(jpql, Car.class).getResultList();
    }

    /**
     * 3. b) Use of aggregation functions in condition using NamedQuery.
     */
    public List<Car> findCarsWithTrunkCapacityGreaterThanAverageNamedQuery() {
        return entityManager.createNamedQuery("Car.findCapacityGreaterThanAverage", Car.class)
                .getResultList();
    }

    /**
     * 3. c) Use of aggregation functions in condition using Criteria API.
     */
    public List<Car> findCarsWithTrunkCapacityGreaterThanAverageCriteria() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Car> mainQuery = cb.createQuery(Car.class);
        Root<Car> root = mainQuery.from(Car.class);

        Subquery<Double> subquery = mainQuery.subquery(Double.class);
        Root<Car> subRoot = subquery.from(Car.class);

        subquery.select(cb.avg(subRoot.get("trunkCapacity")));

        mainQuery.select(root)
                .where(cb.greaterThan(root.get("trunkCapacity"), subquery));

        return entityManager.createQuery(mainQuery).getResultList();
    }

    /**
     * 3. d) Use of aggregation functions in condition using Native Query.
     */
    @SuppressWarnings("unchecked")
    public List<Car> findCarsWithTrunkCapacityGreaterThanAverageNative() {
        String sql = "SELECT v.id, v.license_plate, v.status, v.vehicle_type, c.vehicle_id, c.model, c.trunk_capacity " +
                "FROM vehicles v " +
                "JOIN cars c ON v.id = c.vehicle_id " +
                "WHERE c.trunk_capacity > (SELECT AVG(trunk_capacity) FROM cars)";

        return entityManager.createNativeQuery(sql, Car.class).getResultList();
    }

    /**
     * 3. e) Use of aggregation functions in condition using JOOQ.
     */
    public List<Car> findCarsWithTrunkCapacityGreaterThanAverageJooq() {
        return dsl.select(
                        VEHICLES.ID,
                        VEHICLES.LICENSE_PLATE,
                        VEHICLES.STATUS,
                        VEHICLES.VEHICLE_TYPE,
                        CARS.VEHICLE_ID,
                        CARS.MODEL,
                        CARS.TRUNK_CAPACITY
                )
                .from(VEHICLES)
                .join(CARS).on(VEHICLES.ID.eq(CARS.VEHICLE_ID))
                .where(CARS.TRUNK_CAPACITY.gt(
                        dsl.select(avg(CARS.TRUNK_CAPACITY).cast(Double.class)).from(CARS)
                ))
                .fetchInto(Car.class);
    }

    /**
     * 4. a) Use of aggregation functions in result using JPQL.
     * Calculates the total trunk capacity of all cars with a specific status.
     */
    public Double calculateTotalTrunkCapacityByStatusJpql(String status) {
        String jpql = "SELECT SUM(c.trunkCapacity) FROM Car c WHERE c.status = :status";

        Double totalCapacity = entityManager.createQuery(jpql, Double.class)
                .setParameter("status", status)
                .getSingleResult();

        return totalCapacity != null ? totalCapacity : 0.0;
    }

    /**
     * 4. b) Use of aggregation functions in result using NamedQuery.
     */
    public Double calculateTotalTrunkCapacityByStatusNamedQuery(String status) {
        Double totalCapacity = entityManager.createNamedQuery("Car.calculateTotalCapacityByStatus", Double.class)
                .setParameter("status", status)
                .getSingleResult();

        return totalCapacity != null ? totalCapacity : 0.0;
    }

    /**
     * 4. c) Use of aggregation functions in result using Criteria API.
     * Calculates the total trunk capacity for cars with a specific status.
     */
    public Double calculateTotalTrunkCapacityByStatusCriteria(String status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Double> query = cb.createQuery(Double.class);
        Root<Car> root = query.from(Car.class);

        query.select(cb.sum(root.get("trunkCapacity")))
                .where(cb.equal(root.get("status"), status));

        Double result = entityManager.createQuery(query).getSingleResult();

        return result != null ? result : 0.0;
    }

    /**
     * 4. d) Use of aggregation functions in result using Native Query.
     */
    public Double calculateTotalTrunkCapacityByStatusNative(String status) {
        String sql = """
                SELECT SUM(c.trunk_capacity) FROM vehicles v
                JOIN cars c ON v.id = c.vehicle_id
                WHERE v.status = :status
                """;

        Object result = entityManager.createNativeQuery(sql)
                .setParameter("status", status)
                .getSingleResult();

        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    /**
     * 4. e) Use of aggregation functions in result using JOOQ.
     */
    public Double calculateTotalTrunkCapacityByStatusJooq(String status) {
        Double result = dsl.select(sum(CARS.TRUNK_CAPACITY))
                .from(VEHICLES)
                .join(CARS).on(VEHICLES.ID.eq(CARS.VEHICLE_ID))
                .where(VEHICLES.STATUS.eq(status))
                .fetchOneInto(Double.class);

        return result != null ? result : 0.0;
    }

    /**
     * 5. a) Search with JOIN using JPQL.
     * Retrieves a list of CarDetailsDto for cars with a specific status.
     */
    public List<CarDetailsDto> findCarDetailsJpql(String status) {
        String jpql = """
                SELECT new org.example.sbdbaspectscourse.dto.CarDetailsDto(
                    c.licensePlate,
                    c.model,
                    s.averageRating,
                    s.totalFeedbacks
                )
                FROM Car c
                JOIN c.vehicleFinalStatistic s
                WHERE c.status = :status
                """;

        return entityManager.createQuery(jpql, CarDetailsDto.class)
                .setParameter("status", status)
                .getResultList();
    }

    /**
     * 5. b) Search with JOIN using NamedQuery.
     */
    public List<CarDetailsDto> findCarDetailsNamedQuery(String status) {
        return entityManager.createNamedQuery("Car.findVehicleDetailsNamed", CarDetailsDto.class)
                .setParameter("status", status)
                .getResultList();
    }

    /**
     * 5. c) Search with JOIN using Criteria API.
     * Programmatically joins tables and maps the result into a CarDetailsDto.
     */
    public List<CarDetailsDto> findCarDetailsCriteria(String status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CarDetailsDto> query = cb.createQuery(CarDetailsDto.class);

        Root<Car> carRoot = query.from(Car.class);

        Join<Car, VehicleFinalStatistic> statsJoin = carRoot.join("vehicleFinalStatistic");

        query.select(cb.construct(
                CarDetailsDto.class,
                carRoot.get("licensePlate"),
                carRoot.get("model"),
                statsJoin.get("averageRating"),
                statsJoin.get("totalFeedbacks")
        ));

        query.where(cb.equal(carRoot.get("status"), status));

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * 5. d) Search with JOIN using Native Query.
     * Manually maps raw database rows to CarDetailsDto.
     */
    @SuppressWarnings("unchecked")
    public List<CarDetailsDto> findCarDetailsNative(String status) {
        String sql = """
                SELECT v.license_plate, c.model, s.average_rating, s.total_feedbacks
                FROM vehicles v
                JOIN cars c ON v.id = c.vehicle_id
                JOIN vehicle_final_stats s ON v.id = s.vehicle_id
                WHERE v.status = :status
                """;

        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("status", status)
                .getResultList();

        return results.stream()
                .map(row -> new CarDetailsDto(
                        (String) row[0],
                        (String) row[1],
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                        row[3] != null ? ((Number) row[3]).intValue() : 0
                ))
                .toList();
    }

    /**
     * 5. e) Search with JOIN using JOOQ.
     * Utilizes JOOQ's fluent API to perform joins and automatically maps results into CarDetailsDto.
     */
    public List<CarDetailsDto> findCarDetailsJooq(String status) {
        return dsl.select(
                        VEHICLES.LICENSE_PLATE,
                        CARS.MODEL,
                        VEHICLE_FINAL_STATS.AVERAGE_RATING,
                        VEHICLE_FINAL_STATS.TOTAL_FEEDBACKS
                )
                .from(VEHICLES)
                .join(CARS).on(VEHICLES.ID.eq(CARS.VEHICLE_ID))
                .join(VEHICLE_FINAL_STATS).on(VEHICLES.ID.eq(VEHICLE_FINAL_STATS.VEHICLE_ID))
                .where(VEHICLES.STATUS.eq(status))
                .fetchInto(CarDetailsDto.class);
    }

    /**
     * 6. a) Dynamic query using JPQL (StringBuilder).
     * Constructs a JPQL query dynamically based on provided parameters for status, model, and minimum trunk capacity.
     */
    public List<Car> findCarsDynamicJpql(String status, String model, Double minTrunkCapacity) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Car c WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();

        if (status != null && !status.isBlank()) {
            jpql.append(" AND c.status = :status");
            parameters.put("status", status);
        }
        if (model != null && !model.isBlank()) {
            jpql.append(" AND c.model LIKE :model");
            parameters.put("model", "%" + model + "%");
        }
        if (minTrunkCapacity != null) {
            jpql.append(" AND c.trunkCapacity >= :minTrunkCapacity");
            parameters.put("minTrunkCapacity", minTrunkCapacity);
        }

        var query = entityManager.createQuery(jpql.toString(), Car.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

    /**
     * 6. c) Dynamic query using Criteria API.
     * Programmatically builds a CriteriaQuery based on which parameters are provided, allowing for flexible search criteria.
     */
    public List<Car> findCarsDynamicCriteria(String status, String model, Double minTrunkCapacity) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Car> query = cb.createQuery(Car.class);
        Root<Car> root = query.from(Car.class);

        List<Predicate> predicates = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (model != null && !model.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("model")), "%" + model.toLowerCase() + "%"));
        }
        if (minTrunkCapacity != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trunkCapacity"), minTrunkCapacity));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * 6. d) Dynamic query using Native Query.
     * Constructs a native SQL query dynamically based on provided parameters and maps the results to Car entities.
     */
    @SuppressWarnings("unchecked")
    public List<Car> findCarsDynamicNative(String status, String model, Double minTrunkCapacity) {
        StringBuilder sql = new StringBuilder("""
                SELECT v.id, v.license_plate, v.status, v.vehicle_type, c.vehicle_id, c.model, c.trunk_capacity
                FROM vehicles v
                JOIN cars c ON v.id = c.vehicle_id
                WHERE 1=1
                """);

        Map<String, Object> params = new HashMap<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND v.status = :status");
            params.put("status", status);
        }
        if (model != null && !model.isBlank()) {
            sql.append(" AND c.model ILIKE :model"); // Using ILIKE for case-insensitive search in PostgreSQL
            params.put("model", "%" + model + "%");
        }
        if (minTrunkCapacity != null) {
            sql.append(" AND c.trunk_capacity >= :minTrunkCapacity");
            params.put("minTrunkCapacity", minTrunkCapacity);
        }

        Query query = entityManager.createNativeQuery(sql.toString(), Car.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    /**
     * 6. e) Dynamic query using JOOQ.
     * Utilizes JOOQ's fluent API to build a dynamic query based on which parameters are provided,
     * allowing for flexible search criteria while automatically handling SQL generation and result mapping.
     */
    public List<Car> findCarsDynamicJooq(String status, String model, Double minTrunkCapacity) {
        Condition condition = DSL.noCondition();

        if (status != null && !status.isBlank()) {
            condition = condition.and(VEHICLES.STATUS.eq(status));
        }
        if (model != null && !model.isBlank()) {
            condition = condition.and(CARS.MODEL.containsIgnoreCase(model));
        }
        if (minTrunkCapacity != null) {
            condition = condition.and(CARS.TRUNK_CAPACITY.ge(minTrunkCapacity));
        }

        return dsl.select(VEHICLES.fields())
                .select(CARS.fields())
                .from(VEHICLES)
                .join(CARS).on(VEHICLES.ID.eq(CARS.VEHICLE_ID))
                .where(condition)
                .fetchInto(Car.class);
    }
}