package org.example.sbdbaspectscourse.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ReplicaRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DbContextHolder.getDbType();
        return type == null ? DataSourceType.PRIMARY : type;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return super.getConnection();
        } catch (SQLException e) {
            if (DbContextHolder.getDbType() == DataSourceType.REPLICA) {
                System.err.println("Replica connection failed, routing to PRIMARY");
                DbContextHolder.setDbType(DataSourceType.PRIMARY);
                return super.getConnection();
            }
            throw e;
        }
    }
}