package id.ketemu.dao;

import id.ketemu.config.Db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/** Runs several DAO calls on one connection inside a single transaction. */
public final class Tx {

    private static final ThreadLocal<Connection> CURRENT = new ThreadLocal<>();

    private Tx() {
    }

    static Connection current() {
        return CURRENT.get();
    }

    public static <T> T run(Supplier<T> work) {
        if (CURRENT.get() != null) {
            return work.get();
        }
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            CURRENT.set(c);
            try {
                T result = work.get();
                c.commit();
                return result;
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                CURRENT.remove();
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataException("Transaction failed", e);
        }
    }

    public static void run(Runnable work) {
        run(() -> {
            work.run();
            return null;
        });
    }
}
