package id.ketemu.dao;

import id.ketemu.config.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared JDBC plumbing. Every query goes through a PreparedStatement with
 * bound parameters, and every resource is closed with try-with-resources.
 */
public abstract class BaseDao {

    @FunctionalInterface
    protected interface SqlWork<T> {
        T apply(Connection c) throws SQLException;
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected <T> T with(SqlWork<T> work) {
        Connection tx = Tx.current();
        try {
            if (tx != null) {
                return work.apply(tx);
            }
            try (Connection c = Db.get()) {
                return work.apply(c);
            }
        } catch (SQLException e) {
            throw new DataException(e.getMessage(), e);
        }
    }

    protected static void bind(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a == null) {
                ps.setNull(i + 1, Types.NULL);
            } else if (a instanceof byte[] bytes) {
                ps.setBytes(i + 1, bytes);
            } else {
                ps.setObject(i + 1, a);
            }
        }
    }

    protected <T> List<T> list(String sql, RowMapper<T> mapper, Object... args) {
        return with(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                bind(ps, args);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> out = new ArrayList<>();
                    while (rs.next()) {
                        out.add(mapper.map(rs));
                    }
                    return out;
                }
            }
        });
    }

    protected <T> T one(String sql, RowMapper<T> mapper, Object... args) {
        List<T> rows = list(sql, mapper, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    protected int update(String sql, Object... args) {
        return with(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                bind(ps, args);
                return ps.executeUpdate();
            }
        });
    }

    protected int insert(String sql, Object... args) {
        return with(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(ps, args);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : 0;
                }
            }
        });
    }

    protected long count(String sql, Object... args) {
        Long n = one(sql, rs -> rs.getLong(1), args);
        return n == null ? 0 : n;
    }

    protected static LocalDateTime ts(ResultSet rs, String col) throws SQLException {
        return rs.getObject(col, LocalDateTime.class);
    }

    protected static Integer intOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
}
