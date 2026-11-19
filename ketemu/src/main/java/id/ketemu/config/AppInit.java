package id.ketemu.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates the schema and loads the seed data the first time the app starts
 * against an empty database. Seeded accounts get the demo password hashed
 * with bcrypt here, so no plain-text password lives in the SQL files.
 */
@WebListener
public class AppInit implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppInit.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        int attempts = 0;
        while (true) {
            try {
                Db.init();
                migrate();
                return;
            } catch (Exception e) {
                attempts++;
                LOG.log(Level.WARNING, "Database not ready (attempt " + attempts + "): " + e.getMessage());
                Db.close();
                if (attempts >= 10) {
                    LOG.log(Level.SEVERE, "Giving up on database setup", e);
                    return;
                }
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Db.close();
    }

    private void migrate() throws SQLException, IOException {
        try (Connection c = Db.get()) {
            boolean fresh = !tableExists(c, "users");
            runScript(c, "/db/schema.sql");
            if (fresh) {
                LOG.info("Empty database, loading seed data");
                runScript(c, "/db/seed.sql");
            }
            hashPendingPasswords(c);
        }
    }

    private boolean tableExists(Connection c, String table) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            ps.setString(1, "public." + table);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }

    private void hashPendingPasswords(Connection c) throws SQLException {
        String demo = Db.env("DEMO_PASSWORD", "ketemu123");
        String hash = BCrypt.hashpw(demo, BCrypt.gensalt(10));
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE password_hash = 'PENDING'")) {
            ps.setString(1, hash);
            int n = ps.executeUpdate();
            if (n > 0) {
                LOG.info("Set the demo password for " + n + " seeded accounts");
            }
        }
    }

    private void runScript(Connection c, String resource) throws SQLException, IOException {
        String sql;
        try (InputStream in = AppInit.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing resource " + resource);
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(false);
        try (Statement st = c.createStatement()) {
            for (String stmt : split(sql)) {
                st.execute(stmt);
            }
            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(auto);
        }
    }

    /** Splits a script on semicolons that end a line, skipping comment lines. */
    static List<String> split(String sql) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String line : sql.split("\\R")) {
            String t = line.trim();
            if (t.startsWith("--") || t.isEmpty()) {
                continue;
            }
            cur.append(line).append('\n');
            if (t.endsWith(";")) {
                String s = cur.toString().trim();
                out.add(s.substring(0, s.length() - 1));
                cur.setLength(0);
            }
        }
        if (!cur.toString().isBlank()) {
            out.add(cur.toString().trim());
        }
        return out;
    }
}
