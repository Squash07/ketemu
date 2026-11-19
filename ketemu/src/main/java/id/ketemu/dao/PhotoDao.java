package id.ketemu.dao;

import id.ketemu.model.ItemPhoto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PhotoDao extends BaseDao {

    /** Photo bytes with the fields needed to serve them. */
    public static class Blob {
        public int id;
        public int itemId;
        public String mimeType;
        public byte[] content;
        public long version;
    }

    private static ItemPhoto map(ResultSet rs) throws SQLException {
        ItemPhoto p = new ItemPhoto();
        p.setId(rs.getInt("id"));
        p.setItemId(rs.getInt("item_id"));
        p.setMimeType(rs.getString("mime_type"));
        p.setSortOrder(rs.getInt("sort_order"));
        p.setCreatedAt(ts(rs, "created_at"));
        return p;
    }

    public List<ItemPhoto> listByItem(int itemId) {
        return list("SELECT id, item_id, mime_type, sort_order, created_at FROM item_photos WHERE item_id = ? ORDER BY sort_order, id",
                PhotoDao::map, itemId);
    }

    public ItemPhoto findMeta(int id) {
        return one("SELECT id, item_id, mime_type, sort_order, created_at FROM item_photos WHERE id = ?", PhotoDao::map, id);
    }

    public Blob load(int id) {
        return one("SELECT id, item_id, mime_type, content, extract(epoch FROM created_at)::bigint AS version FROM item_photos WHERE id = ?", rs -> {
            Blob b = new Blob();
            b.id = rs.getInt("id");
            b.itemId = rs.getInt("item_id");
            b.mimeType = rs.getString("mime_type");
            b.content = rs.getBytes("content");
            b.version = rs.getLong("version");
            return b;
        }, id);
    }

    public long countByItem(int itemId) {
        return count("SELECT count(*) FROM item_photos WHERE item_id = ?", itemId);
    }

    public int add(int itemId, byte[] content, String mime) {
        return insert("INSERT INTO item_photos (item_id, content, mime_type, sort_order) "
                        + "VALUES (?, ?, ?, (SELECT coalesce(max(sort_order) + 1, 0) FROM item_photos WHERE item_id = ?))",
                itemId, content, mime, itemId);
    }

    /** Replacing bumps created_at so the ETag changes and browsers fetch the new bytes. */
    public void replace(int id, byte[] content, String mime) {
        update("UPDATE item_photos SET content = ?, mime_type = ?, created_at = now() WHERE id = ?", content, mime, id);
    }

    public void makeCover(int itemId, int photoId) {
        update("UPDATE item_photos SET sort_order = CASE WHEN id = ? THEN 0 ELSE sort_order + 1 END WHERE item_id = ?", photoId, itemId);
    }

    public void delete(int id) {
        update("DELETE FROM item_photos WHERE id = ?", id);
    }
}
