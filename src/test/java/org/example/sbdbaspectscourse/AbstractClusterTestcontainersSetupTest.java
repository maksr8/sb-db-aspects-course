package org.example.sbdbaspectscourse;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;

public abstract class AbstractClusterTestcontainersSetupTest {

    protected static final Network network = Network.newNetwork();

    public static final GenericContainer<?> primary = new GenericContainer<>("bitnami/postgresql@sha256:7a695433df7728a4efa0c66310f4e9e71a37f3fbbc1bb179b8308896dff40a6c")
            .withNetwork(network)
            .withNetworkAliases("primary")
            .withExposedPorts(5432)
            .withEnv("POSTGRESQL_REPLICATION_MODE", "master")
            .withEnv("POSTGRESQL_REPLICATION_USER", "replicator")
            .withEnv("POSTGRESQL_REPLICATION_PASSWORD", "repl_password")
            .withEnv("POSTGRESQL_USERNAME", "postgres")
            .withEnv("POSTGRESQL_PASSWORD", "root")
            .withEnv("POSTGRESQL_DATABASE", "taxi_db")
            .withEnv("POSTGRESQL_SYNCHRONOUS_COMMIT_MODE", "on")
            .withEnv("POSTGRESQL_NUM_SYNCHRONOUS_REPLICAS", "1")
            .withStartupTimeout(Duration.ofMinutes(2));

    public static final GenericContainer<?> replica = new GenericContainer<>("bitnami/postgresql@sha256:7a695433df7728a4efa0c66310f4e9e71a37f3fbbc1bb179b8308896dff40a6c")
            .withNetwork(network)
            .withNetworkAliases("replica")
            .withExposedPorts(5432)
            .dependsOn(primary)
            .withEnv("POSTGRESQL_REPLICATION_MODE", "slave")
            .withEnv("POSTGRESQL_REPLICATION_USER", "replicator")
            .withEnv("POSTGRESQL_REPLICATION_PASSWORD", "repl_password")
            .withEnv("POSTGRESQL_MASTER_HOST", "primary")
            .withEnv("POSTGRESQL_MASTER_PORT_NUMBER", "5432")
            .withEnv("POSTGRESQL_USERNAME", "postgres")
            .withEnv("POSTGRESQL_PASSWORD", "root")
            .withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    public static void startContainersAndMigrate() {
        primary.start();
        waitForDatabase(primary.getHost(), primary.getMappedPort(5432));

        replica.start();
        waitForDatabase(replica.getHost(), replica.getMappedPort(5432));

        String primaryUrl = String.format("jdbc:postgresql://%s:%d/taxi_db", primary.getHost(), primary.getMappedPort(5432));
        
        Flyway flyway = Flyway.configure()
                .dataSource(primaryUrl, "postgres", "root")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    private static void waitForDatabase(String host, int port) {
        String url = String.format("jdbc:postgresql://%s:%d/taxi_db", host, port);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 90000) {
            try (Connection conn = DriverManager.getConnection(url, "postgres", "root")) {
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Database connection timeout: " + url);
    }

    @AfterAll
    public static void stopContainers() {
        primary.stop();
        replica.stop();
        network.close();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.primary.jdbc-url", () -> String.format("jdbc:postgresql://%s:%d/taxi_db", primary.getHost(), primary.getMappedPort(5432)));
        registry.add("spring.datasource.primary.username", () -> "postgres");
        registry.add("spring.datasource.primary.password", () -> "root");

        registry.add("spring.datasource.replica.jdbc-url", () -> String.format("jdbc:postgresql://%s:%d/taxi_db?connectTimeout=2", replica.getHost(), replica.getMappedPort(5432)));
        registry.add("spring.datasource.replica.username", () -> "postgres");
        registry.add("spring.datasource.replica.password", () -> "root");
        registry.add("spring.datasource.replica.connection-timeout", () -> "3000");
        registry.add("spring.datasource.replica.validation-timeout", () -> "1000");

        registry.add("spring.flyway.enabled", () -> "false");
    }
}