package id.ketemu.dao;

import id.ketemu.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao extends BaseDao {

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNrp(rs.getString("nrp"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(ts(rs, "created_at"));
        return u;
    }

    public User findById(int id) {
        return one("SELECT * FROM users WHERE id = ?", UserDao::map, id);
    }

    public User findByEmail(String email) {
        return one("SELECT * FROM users WHERE lower(email) = lower(?)", UserDao::map, email);
    }

    public boolean emailTaken(String email, int exceptId) {
        return count("SELECT count(*) FROM users WHERE lower(email) = lower(?) AND id <> ?", email, exceptId) > 0;
    }

    public boolean nrpTaken(String nrp, int exceptId) {
        return count("SELECT count(*) FROM users WHERE nrp = ? AND id <> ?", nrp, exceptId) > 0;
    }

    public int create(User u) {
        return insert("INSERT INTO users (nrp, full_name, email, password_hash, phone, role) VALUES (?, ?, ?, ?, ?, 'student')",
                u.getNrp(), u.getFullName(), u.getEmail(), u.getPasswordHash(), u.getPhone());
    }

    public void updateProfile(User u) {
        update("UPDATE users SET nrp = ?, full_name = ?, email = ?, phone = ? WHERE id = ?",
                u.getNrp(), u.getFullName(), u.getEmail(), u.getPhone(), u.getId());
    }

    public void updatePassword(int id, String hash) {
        update("UPDATE users SET password_hash = ? WHERE id = ?", hash, id);
    }

    public void updateRole(int id, String role) {
        update("UPDATE users SET role = ? WHERE id = ?", role, id);
    }

    public void delete(int id) {
        update("DELETE FROM users WHERE id = ?", id);
    }

    public long countAdmins() {
        return count("SELECT count(*) FROM users WHERE role = 'admin'");
    }

    public List<User> search(String q, int limit, int offset) {
        String like = "%" + (q == null ? "" : q.trim()) + "%";
        return list("SELECT * FROM users WHERE full_name ILIKE ? OR email ILIKE ? OR nrp ILIKE ? "
                + "ORDER BY role, full_name LIMIT ? OFFSET ?", UserDao::map, like, like, like, limit, offset);
    }

    public long countSearch(String q) {
        String like = "%" + (q == null ? "" : q.trim()) + "%";
        return count("SELECT count(*) FROM users WHERE full_name ILIKE ? OR email ILIKE ? OR nrp ILIKE ?", like, like, like);
    }

    /** Items posted and items returned, for the public mini-profile. */
    public long[] stats(int userId) {
        long posted = count("SELECT count(*) FROM items WHERE reporter_id = ?", userId);
        long returned = count("SELECT count(*) FROM items WHERE reporter_id = ? AND status = 'returned'", userId);
        return new long[]{posted, returned};
    }
}
