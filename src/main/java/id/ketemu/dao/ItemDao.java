package id.ketemu.dao;

import id.ketemu.model.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemDao extends BaseDao {

    private static final String SELECT =
            "SELECT i.*, u.full_name AS reporter_name, u.created_at AS reporter_since, "
            + "c.name AS category_name, c.icon AS category_icon, l.name AS location_name, h.name AS held_at_name, "
            + "(SELECT p.id FROM item_photos p WHERE p.item_id = i.id ORDER BY p.sort_order, p.id LIMIT 1) AS cover_photo_id, "
            + "(SELECT count(*) FROM item_photos p WHERE p.item_id = i.id) AS photo_count "
            + "FROM items i "
            + "JOIN users u ON u.id = i.reporter_id "
            + "JOIN categories c ON c.id = i.category_id "
            + "JOIN locations l ON l.id = i.location_id "
            + "LEFT JOIN locations h ON h.id = i.held_at_id ";

    /** Board filters. Empty values mean "any". */
    public static class Filter {
        public String kind;
        public Integer categoryId;
        public Integer locationId;
        public String q;
        public Integer reporterId;
        public boolean includeClosed;

        public String getKind() {
            return kind;
        }

        public Integer getCategoryId() {
            return categoryId;
        }

        public Integer getLocationId() {
            return locationId;
        }

        public String getQ() {
            return q;
        }
    }

    static Item map(ResultSet rs) throws SQLException {
        Item i = new Item();
        i.setId(rs.getInt("id"));
        i.setCode(rs.getString("code"));
        i.setReporterId(rs.getInt("reporter_id"));
        i.setReporterName(rs.getString("reporter_name"));
        i.setReporterSince(ts(rs, "reporter_since"));
        i.setCategoryId(rs.getInt("category_id"));
        i.setCategoryName(rs.getString("category_name"));
        i.setCategoryIcon(rs.getString("category_icon"));
        i.setLocationId(rs.getInt("location_id"));
        i.setLocationName(rs.getString("location_name"));
        i.setKind(rs.getString("kind"));
        i.setTitle(rs.getString("title"));
        i.setDescription(rs.getString("description"));
        i.setLocationDetail(rs.getString("location_detail"));
        i.setEventDate(rs.getObject("event_date", LocalDate.class));
        i.setHeldAtId(intOrNull(rs, "held_at_id"));
        i.setHeldAtName(rs.getString("held_at_name"));
        i.setStatus(rs.getString("status"));
        i.setCreatedAt(ts(rs, "created_at"));
        i.setUpdatedAt(ts(rs, "updated_at"));
        i.setCoverPhotoId(intOrNull(rs, "cover_photo_id"));
        i.setPhotoCount(rs.getInt("photo_count"));
        return i;
    }

    private static String where(Filter f, List<Object> args, boolean withKind) {
        StringBuilder w = new StringBuilder("WHERE 1 = 1 ");
        if (!f.includeClosed) {
            w.append("AND i.status <> 'closed' ");
        }
        if (withKind && f.kind != null && !f.kind.isEmpty()) {
            w.append("AND i.kind = ? ");
            args.add(f.kind);
        }
        if (f.categoryId != null) {
            w.append("AND i.category_id = ? ");
            args.add(f.categoryId);
        }
        if (f.locationId != null) {
            w.append("AND i.location_id = ? ");
            args.add(f.locationId);
        }
        if (f.reporterId != null) {
            w.append("AND i.reporter_id = ? ");
            args.add(f.reporterId);
        }
        if (f.q != null && !f.q.isBlank()) {
            w.append("AND (i.title ILIKE ? OR i.description ILIKE ? OR i.code ILIKE ? OR i.location_detail ILIKE ?) ");
            String like = "%" + f.q.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        return w.toString();
    }

    public List<Item> search(Filter f, int limit, int offset) {
        List<Object> args = new ArrayList<>();
        String sql = SELECT + where(f, args, true)
                + "ORDER BY CASE i.status WHEN 'open' THEN 0 WHEN 'claimed' THEN 1 ELSE 2 END, i.created_at DESC, i.id DESC LIMIT ? OFFSET ?";
        args.add(limit);
        args.add(offset);
        return list(sql, ItemDao::map, args.toArray());
    }

    public long countSearch(Filter f) {
        List<Object> args = new ArrayList<>();
        String sql = "SELECT count(*) FROM items i " + where(f, args, true);
        return count(sql, args.toArray());
    }

    /** Counts for the All / Lost / Found switch, with every other filter applied. */
    public Map<String, Long> countByKind(Filter f) {
        List<Object> args = new ArrayList<>();
        String sql = "SELECT i.kind, count(*) FROM items i " + where(f, args, false) + "GROUP BY i.kind";
        Map<String, Long> out = new LinkedHashMap<>();
        out.put("lost", 0L);
        out.put("found", 0L);
        for (Object[] row : list(sql, rs -> new Object[]{rs.getString(1), rs.getLong(2)}, args.toArray())) {
            out.put((String) row[0], (Long) row[1]);
        }
        out.put("all", out.get("lost") + out.get("found"));
        return out;
    }

    public Item findById(int id) {
        return one(SELECT + "WHERE i.id = ?", ItemDao::map, id);
    }

    public Item findByCode(String code) {
        return one(SELECT + "WHERE upper(i.code) = upper(?)", ItemDao::map, code);
    }

    public int create(Item it) {
        int id = insert("INSERT INTO items (reporter_id, category_id, location_id, kind, title, description, location_detail, event_date, held_at_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                it.getReporterId(), it.getCategoryId(), it.getLocationId(), it.getKind(), it.getTitle(),
                it.getDescription(), it.getLocationDetail(), it.getEventDate(), it.getHeldAtId());
        update("UPDATE items SET code = ? WHERE id = ?", String.format("K-%04d", id), id);
        return id;
    }

    public void update(Item it) {
        update("UPDATE items SET category_id = ?, location_id = ?, title = ?, description = ?, location_detail = ?, "
                        + "event_date = ?, held_at_id = ?, updated_at = now() WHERE id = ?",
                it.getCategoryId(), it.getLocationId(), it.getTitle(), it.getDescription(), it.getLocationDetail(),
                it.getEventDate(), it.getHeldAtId(), it.getId());
    }

    public void updateStatus(int id, String status) {
        update("UPDATE items SET status = ?, updated_at = now() WHERE id = ?", status, id);
    }

    public void delete(int id) {
        update("DELETE FROM items WHERE id = ?", id);
    }

    public long countReturnedSince(LocalDate since) {
        return count("SELECT count(*) FROM items WHERE status = 'returned' AND updated_at >= ?", since.atStartOfDay());
    }

    public Map<String, Long> countByStatus() {
        Map<String, Long> out = new LinkedHashMap<>();
        for (String s : new String[]{"open", "claimed", "returned", "closed"}) {
            out.put(s, 0L);
        }
        for (Object[] row : list("SELECT status, count(*) FROM items GROUP BY status",
                rs -> new Object[]{rs.getString(1), rs.getLong(2)})) {
            out.put((String) row[0], (Long) row[1]);
        }
        return out;
    }

    public Map<String, Long> countOpenByKind() {
        Map<String, Long> out = new LinkedHashMap<>();
        out.put("lost", 0L);
        out.put("found", 0L);
        for (Object[] row : list("SELECT kind, count(*) FROM items WHERE status = 'open' GROUP BY kind",
                rs -> new Object[]{rs.getString(1), rs.getLong(2)})) {
            out.put((String) row[0], (Long) row[1]);
        }
        return out;
    }

    public List<Item> newest(int limit) {
        return list(SELECT + "ORDER BY i.created_at DESC, i.id DESC LIMIT ?", ItemDao::map, limit);
    }
}
