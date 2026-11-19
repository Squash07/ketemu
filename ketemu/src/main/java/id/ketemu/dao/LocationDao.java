package id.ketemu.dao;

import id.ketemu.model.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LocationDao extends BaseDao {

    private static final String SELECT = "SELECT l.*, (SELECT count(*) FROM items i WHERE i.location_id = l.id) AS item_count FROM locations l ";

    private static Location map(ResultSet rs) throws SQLException {
        Location l = new Location();
        l.setId(rs.getInt("id"));
        l.setName(rs.getString("name"));
        l.setZone(rs.getString("zone"));
        l.setSkkPost(rs.getBoolean("is_skk_post"));
        l.setCreatedAt(ts(rs, "created_at"));
        l.setItemCount(rs.getInt("item_count"));
        return l;
    }

    public List<Location> listAll() {
        return list(SELECT + "ORDER BY l.is_skk_post DESC, l.name", LocationDao::map);
    }

    public List<Location> listPosts() {
        return list(SELECT + "WHERE l.is_skk_post ORDER BY l.name", LocationDao::map);
    }

    public Location findById(int id) {
        return one(SELECT + "WHERE l.id = ?", LocationDao::map, id);
    }

    public boolean nameTaken(String name, int exceptId) {
        return count("SELECT count(*) FROM locations WHERE lower(name) = lower(?) AND id <> ?", name, exceptId) > 0;
    }

    public int create(String name, String zone, boolean skkPost) {
        return insert("INSERT INTO locations (name, zone, is_skk_post) VALUES (?, ?, ?)", name, zone, skkPost);
    }

    public void update(int id, String name, String zone, boolean skkPost) {
        update("UPDATE locations SET name = ?, zone = ?, is_skk_post = ? WHERE id = ?", name, zone, skkPost, id);
    }

    public void delete(int id) {
        update("DELETE FROM locations WHERE id = ?", id);
    }

    /** Items that use this place as where it happened or as the post holding it. */
    public long countHeldItems(int id) {
        return count("SELECT count(*) FROM items WHERE held_at_id = ?", id);
    }
}
