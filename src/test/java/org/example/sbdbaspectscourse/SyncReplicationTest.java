package org.example.sbdbaspectscourse;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class SyncReplicationTest extends AbstractClusterTestcontainersSetupTest{

    private static HikariDataSource primaryDataSource;
    private static HikariDataSource replicaDataSource;

    @BeforeAll
    public static void setUpDataSources() {
        HikariConfig primaryConfig = new HikariConfig();
        primaryConfig.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/taxi_db", primary.getHost(), primary.getMappedPort(5432)));
        primaryConfig.setUsername("postgres");
        primaryConfig.setPassword("root");
        primaryDataSource = new HikariDataSource(primaryConfig);

        HikariConfig replicaConfig = new HikariConfig();
        replicaConfig.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/taxi_db", replica.getHost(), replica.getMappedPort(5432)));
        replicaConfig.setUsername("postgres");
        replicaConfig.setPassword("root");
        replicaDataSource = new HikariDataSource(replicaConfig);
    }

    @AfterAll
    public static void tearDownDataSources() {
        if (primaryDataSource != null) primaryDataSource.close();
        if (replicaDataSource != null) replicaDataSource.close();
    }

    @Test
    public void testInsertSynchronousReplication() throws Exception {
        String uniquePlate = "INS-" + UUID.randomUUID().toString().substring(0, 5);
        boolean foundInReplica = false;

        try (Connection primaryConn = primaryDataSource.getConnection();
             PreparedStatement insertStmt = primaryConn.prepareStatement(
                     "INSERT INTO vehicles (license_plate, status, vehicle_type) VALUES (?, 'AVAILABLE', 'CAR')")) {
            insertStmt.setString(1, uniquePlate);
            insertStmt.executeUpdate();
        }

        try (Connection replicaConn = replicaDataSource.getConnection();
             PreparedStatement selectStmt = replicaConn.prepareStatement(
                     "SELECT count(*) FROM vehicles WHERE license_plate = ?")) {
            selectStmt.setString(1, uniquePlate);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    foundInReplica = rs.getInt(1) > 0;
                }
            }
        }

        Assertions.assertTrue(foundInReplica);
    }

    @Test
    public void testUpdateSynchronousReplication() throws Exception {
        String uniquePlate = "UPD-" + UUID.randomUUID().toString().substring(0, 5);
        String initialStatus = "AVAILABLE";
        String updatedStatus = "IN_TRIP";
        String statusInReplica = null;

        try (Connection primaryConn = primaryDataSource.getConnection();
             PreparedStatement insertStmt = primaryConn.prepareStatement(
                     "INSERT INTO vehicles (license_plate, status, vehicle_type) VALUES (?, ?, 'CAR')")) {
            insertStmt.setString(1, uniquePlate);
            insertStmt.setString(2, initialStatus);
            insertStmt.executeUpdate();
        }

        try (Connection primaryConn = primaryDataSource.getConnection();
             PreparedStatement updateStmt = primaryConn.prepareStatement(
                     "UPDATE vehicles SET status = ? WHERE license_plate = ?")) {
            updateStmt.setString(1, updatedStatus);
            updateStmt.setString(2, uniquePlate);
            updateStmt.executeUpdate();
        }

        try (Connection replicaConn = replicaDataSource.getConnection();
             PreparedStatement selectStmt = replicaConn.prepareStatement(
                     "SELECT status FROM vehicles WHERE license_plate = ?")) {
            selectStmt.setString(1, uniquePlate);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    statusInReplica = rs.getString(1);
                }
            }
        }

        Assertions.assertEquals(updatedStatus, statusInReplica);
    }
}