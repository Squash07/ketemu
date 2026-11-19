package id.ketemu.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection pool built from environment variables.
 * DB_URL accepts either a JDBC URL (jdbc:postgresql://host/db?sslmode=require)
 * or the postgresql://user:pass@host/db form that Neon shows on its dashboard.
 */
public final class Db {

    private static HikariDataSource pool;

    private Db() {
    }

    public static synchronized void init() {
        if (pool != null) {
            return;
        }
        String url = env("DB_URL", "jdbc:postgresql://localhost:5432/ketemu");
        String user = env("DB_USER", "ketemu");
        String password = env("DB_PASSWORD", "ketemu");

        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            URI uri = URI.create(url.replaceFirst("^postgres(ql)?://", "http://"));
            if (uri.getUserInfo() != null) {
                String[] parts = uri.getUserInfo().split(":", 2);
                user = parts[0];
                if (parts.length > 1) {
                    password = parts[1];
                }
            }
            String query = uri.getQuery() == null ? "sslmode=require" : uri.getQuery();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            url = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + "?" + query;
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(password);
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setMaximumPoolSize(Integer.parseInt(env("DB_POOL_SIZE", "5")));
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(20_000);
        cfg.setIdleTimeout(120_000);
        cfg.setMaxLifetime(600_000);
        cfg.setPoolName("ketemu");
        pool = new HikariDataSource(cfg);
    }

    public static Connection get() throws SQLException {
        if (pool == null) {
            init();
        }
        return pool.getConnection();
    }

    public static synchronized void close() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    public static String env(String key, String fallback) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            v = System.getProperty(key);
        }
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }
}
