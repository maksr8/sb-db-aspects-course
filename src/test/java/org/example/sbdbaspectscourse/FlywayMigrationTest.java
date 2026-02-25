package org.example.sbdbaspectscourse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationTest extends AbstractTestcontainersSetupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testMergeAndDropMigrationStrategy() {
        Integer rawCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'raw_feedbacks'", Integer.class);
        Integer tempCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'temp_vehicle_ratings'", Integer.class);

        Assertions.assertEquals(0, rawCount, "Table raw_feedbacks MUST be dropped");
        Assertions.assertEquals(0, tempCount, "Table temp_vehicle_ratings MUST be dropped");

        List<Map<String, Object>> finalStats = jdbcTemplate.queryForList(
                "SELECT vehicle_id, total_feedbacks, average_rating FROM vehicle_final_stats ORDER BY vehicle_id"
        );

        Assertions.assertEquals(2, finalStats.size(), "Should have aggregated stats for 2 vehicles");

        Map<String, Object> carStats = finalStats.getFirst();
        Assertions.assertEquals(1L, ((Number) carStats.get("vehicle_id")).longValue());
        Assertions.assertEquals(3, ((Number) carStats.get("total_feedbacks")).intValue());
        Assertions.assertEquals(4.0, ((Number) carStats.get("average_rating")).doubleValue());
    }
}
