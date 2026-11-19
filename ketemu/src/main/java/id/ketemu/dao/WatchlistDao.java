package id.ketemu.dao;

import id.ketemu.model.Item;
import id.ketemu.model.WatchlistEntry;

import java.util.List;

public class WatchlistDao extends BaseDao {

    public List<WatchlistEntry> listByUser(int userId) {
        String sql = "SELECT w.user_id, w.note, w.created_at AS saved_at, i.*, u.full_name AS reporter_name, u.created_at AS reporter_since, "
                + "c.name AS category_name, c.icon AS category_icon, l.name AS location_name, h.name AS held_at_name, "
                + "(SELECT p.id FROM item_photos p WHERE p.item_id = i.id ORDER BY p.sort_order, p.id LIMIT 1) AS cover_photo_id, "
                + "(SELECT count(*) FROM item_photos p WHERE p.item_id = i.id) AS photo_count "
                + "FROM watchlist w JOIN items i ON i.id = w.item_id "
                + "JOIN users u ON u.id = i.reporter_id JOIN categories c ON c.id = i.category_id "
                + "JOIN locations l ON l.id = i.location_id LEFT JOIN locations h ON h.id = i.held_at_id "
                + "WHERE w.user_id = ? ORDER BY w.created_at DESC";
        return list(sql, rs -> {
            Item item = ItemDao.map(rs);
            WatchlistEntry w = new WatchlistEntry();
            w.setUserId(rs.getInt("user_id"));
            w.setItemId(item.getId());
            w.setNote(rs.getString("note"));
            w.setCreatedAt(ts(rs, "saved_at"));
            w.setItem(item);
            return w;
        }, userId);
    }

    public boolean isWatching(int userId, int itemId) {
        return count("SELECT count(*) FROM watchlist WHERE user_id = ? AND item_id = ?", userId, itemId) > 0;
    }

    public void add(int userId, int itemId, String note) {
        update("INSERT INTO watchlist (user_id, item_id, note) VALUES (?, ?, ?) ON CONFLICT (user_id, item_id) DO NOTHING", userId, itemId, note);
    }

    public int updateNote(int userId, int itemId, String note) {
        return update("UPDATE watchlist SET note = ? WHERE user_id = ? AND item_id = ?", note, userId, itemId);
    }

    public int remove(int userId, int itemId) {
        return update("DELETE FROM watchlist WHERE user_id = ? AND item_id = ?", userId, itemId);
    }

    public List<Integer> watchersOf(int itemId) {
        return list("SELECT user_id FROM watchlist WHERE item_id = ?", rs -> rs.getInt(1), itemId);
    }
}
