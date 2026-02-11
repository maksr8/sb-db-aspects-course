package org.example.sbdbaspectscourse.dao;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.model.Car;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VehicleJdbcDao {
    private final JdbcTemplate jdbcTemplate;

    public List<String> findAllLicensePlates() {
        String sql = "SELECT license_plate FROM vehicles";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("license_plate"));
    }

    public int updateStatus(Long id, String newStatus) {
        String sql = "UPDATE vehicles SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newStatus, id);
    }

    public List<Car> findAllCarsDetailed() {
        String sql = """
            SELECT v.id, v.license_plate, v.status, c.model, c.trunk_capacity 
            FROM vehicles v 
            JOIN cars c ON v.id = c.vehicle_id
        """;

        RowMapper<Car> rowMapper = (rs, rowNum) -> {
            Car car = new Car();
            car.setId(rs.getLong("id"));
            car.setLicensePlate(rs.getString("license_plate"));
            car.setStatus(rs.getString("status"));
            car.setModel(rs.getString("model"));
            car.setTrunkCapacity(rs.getDouble("trunk_capacity"));
            return car;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
}
