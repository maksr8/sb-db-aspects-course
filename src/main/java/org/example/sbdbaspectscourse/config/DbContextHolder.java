package org.example.sbdbaspectscourse.config;

public class DbContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    public static void setDbType(DataSourceType type) {
        CONTEXT.set(type);
    }

    public static DataSourceType getDbType() {
        return CONTEXT.get();
    }

    public static void clearDbType() {
        CONTEXT.remove();
    }
}