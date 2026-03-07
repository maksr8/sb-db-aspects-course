package org.example.sbdbaspectscourse;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLTransientConnectionException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class PostgresConnectionsExhaustionTest {

    static {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testDbConnectionsExhaustion() throws Exception {
        try (GenericContainer<?> limitedDb = new GenericContainer<>("postgres:18.3")
                .withExposedPorts(5432)
                .withEnv("POSTGRES_USER", "postgres")
                .withEnv("POSTGRES_PASSWORD", "root")
                .withEnv("POSTGRES_DB", "taxi_db")
                .withCommand("postgres", "-c", "max_connections=5")
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(1))) {

            limitedDb.start();
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/taxi_db", limitedDb.getHost(), limitedDb.getMappedPort(5432));

            try (Connection conn = java.sql.DriverManager.getConnection(jdbcUrl, "postgres", "root");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW max_connections")) {
                if (rs.next()) {
                    Assertions.assertEquals("5", rs.getString(1));
                }
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername("postgres");
            config.setPassword("root");
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(2000);

            try (HikariDataSource dataSource = new HikariDataSource(config)) {
                Assertions.assertThrows(
                        SQLTransientConnectionException.class,
                        () -> exhaustPool(dataSource, 10)
                );
            }
        }
    }

    private void exhaustPool(HikariDataSource dataSource, int amount) throws Exception {
        List<Connection> activeConnections = new ArrayList<>();
        try {
            for (int i = 0; i < amount; i++) {
                activeConnections.add(dataSource.getConnection());
            }
        } finally {
            for (Connection conn : activeConnections) {
                if (conn != null) {
                    conn.close();
                }
            }
        }
    }
}