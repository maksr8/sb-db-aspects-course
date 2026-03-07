package org.example.sbdbaspectscourse;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

public class HikariPoolExhaustionTest extends AbstractClusterTestcontainersSetupTest {

    @Test
    public void testHikariPoolExhaustion() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/taxi_db", primary.getHost(), primary.getMappedPort(5432)));
        config.setUsername("postgres");
        config.setPassword("root");
        config.setMaximumPoolSize(1);
        config.setConnectionTimeout(2000);

        try (HikariDataSource exhaustDataSource = new HikariDataSource(config)) {

            CompletableFuture<Void> blockerThread = CompletableFuture.runAsync(() -> {
                try (Connection conn = exhaustDataSource.getConnection()) {
                    conn.setAutoCommit(false);
                    Thread.sleep(5000);
                    conn.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            Thread.sleep(500);

            Assertions.assertThrows(
                    java.sql.SQLTransientConnectionException.class,
                    exhaustDataSource::getConnection,
                    "Expected Hikari to throw SQLTransientConnectionException when pool is exhausted"
            );

            blockerThread.get();
        }
    }
}