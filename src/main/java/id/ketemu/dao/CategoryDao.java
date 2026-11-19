package id.ketemu.dao;

import id.ketemu.model.Category;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CategoryDao extends BaseDao {

    private static final String SELECT = "SELECT c.*, (SELECT count(*) FROM items i WHERE i.category_id = c.id) AS item_count FROM categories c ";

    private static Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setIcon(rs.getString("icon"));
        c.setCreatedAt(ts(rs, "created_at"));
        c.setItemCount(rs.getInt("item_count"));
        return c;
    }

    public List<Category> listAll() {
        return list(SELECT + "ORDER BY c.name", CategoryDao::map);
    }

    public Category findById(int id) {
        return one(SELECT + "WHERE c.id = ?", CategoryDao::map, id);
    }

    public boolean nameTaken(String name, int exceptId) {
        return count("SELECT count(*) FROM categories WHERE lower(name) = lower(?) AND id <> ?", name, exceptId) > 0;
    }

    public int create(String name, String icon) {
        return insert("INSERT INTO categories (name, icon) VALUES (?, ?)", name, icon);
    }

    public void update(int id, String name, String icon) {
        update("UPDATE categories SET name = ?, icon = ? WHERE id = ?", name, icon, id);
    }

    public void delete(int id) {
        update("DELETE FROM categories WHERE id = ?", id);
    }
}
