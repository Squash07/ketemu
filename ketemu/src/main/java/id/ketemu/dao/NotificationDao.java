package id.ketemu.dao;

import id.ketemu.model.Notification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NotificationDao extends BaseDao {

    private static Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setItemId(intOrNull(rs, "item_id"));
        n.setItemCode(rs.getString("item_code"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(ts(rs, "created_at"));
        return n;
    }

    public List<Notification> listByUser(int userId, int limit, int offset) {
        return list("SELECT n.*, i.code AS item_code FROM notifications n LEFT JOIN items i ON i.id = n.item_id "
                + "WHERE n.user_id = ? ORDER BY n.created_at DESC, n.id DESC LIMIT ? OFFSET ?", NotificationDao::map, userId, limit, offset);
    }

    public long countByUser(int userId) {
        return count("SELECT count(*) FROM notifications WHERE user_id = ?", userId);
    }

    public long countUnread(int userId) {
        return count("SELECT count(*) FROM notifications WHERE user_id = ? AND NOT is_read", userId);
    }

    public void create(int userId, Integer itemId, String message) {
        String msg = message.length() > 200 ? message.substring(0, 197) + "..." : message;
        update("INSERT INTO notifications (user_id, item_id, message) VALUES (?, ?, ?)", userId, itemId, msg);
    }

    public int markRead(int id, int userId) {
        return update("UPDATE notifications SET is_read = true WHERE id = ? AND user_id = ?", id, userId);
    }

    public int markAllRead(int userId) {
        return update("UPDATE notifications SET is_read = true WHERE user_id = ? AND NOT is_read", userId);
    }

    public int delete(int id, int userId) {
        return update("DELETE FROM notifications WHERE id = ? AND user_id = ?", id, userId);
    }

    public int clear(int userId) {
        return update("DELETE FROM notifications WHERE user_id = ?", userId);
    }
}
