package org.example.sbdbaspectscourse;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostgresMaxConnectionsPerformanceImpactTest {

    static {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testPerformanceImpactOfMaxConnections() throws Exception {
        try (GenericContainer<?> bottleneckDb = new GenericContainer<>("postgres:18.3")
                .withExposedPorts(5432)
                .withEnv("POSTGRES_USER", "postgres")
                .withEnv("POSTGRES_PASSWORD", "root")
                .withEnv("POSTGRES_DB", "taxi_db")
                .withCommand("postgres", "-c", "max_connections=10")
                .waitingFor(Wait.forListeningPort());

             GenericContainer<?> optimalDb = new GenericContainer<>("postgres:18.3")
                     .withExposedPorts(5432)
                     .withEnv("POSTGRES_USER", "postgres")
                     .withEnv("POSTGRES_PASSWORD", "root")
                     .withEnv("POSTGRES_DB", "taxi_db")
                     .waitingFor(Wait.forListeningPort())) {

            bottleneckDb.start();
            optimalDb.start();

            HikariDataSource bottleneckPool = createDataSource(bottleneckDb, 5);
            HikariDataSource optimalPool = createDataSource(optimalDb, 20);

            warmupPool(optimalPool, 20);
            warmupPool(bottleneckPool, 5);

            long optimalTime = runConcurrentWorkload(optimalPool, 20);
            long bottleneckTime = runConcurrentWorkload(bottleneckPool, 20);

            System.out.println("Optimal Pool Time: " + optimalTime + " ms");
            System.out.println("Bottleneck Pool Time: " + bottleneckTime + " ms");
            Assertions.assertTrue(bottleneckTime > optimalTime * 2);

            bottleneckPool.close();
            optimalPool.close();
        }
    }

    private HikariDataSource createDataSource(GenericContainer<?> db, int maxPoolSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/taxi_db", db.getHost(), db.getMappedPort(5432)));
        config.setUsername("postgres");
        config.setPassword("root");
        config.setMaximumPoolSize(maxPoolSize);
        return new HikariDataSource(config);
    }

    private long runConcurrentWorkload(HikariDataSource dataSource, int concurrentUsers) {
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);

        for (int i = 0; i < concurrentUsers; i++) {
            tasks.add(CompletableFuture.runAsync(() -> {
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT pg_sleep(0.1)");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor));
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        return System.currentTimeMillis() - startTime;
    }

    private void warmupPool(HikariDataSource dataSource, int poolSize) throws Exception {
        System.out.println("Warming up pool to " + poolSize + " connections...");
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            connections.add(dataSource.getConnection());
        }
        for (Connection conn : connections) {
            conn.close();
        }
    }
}