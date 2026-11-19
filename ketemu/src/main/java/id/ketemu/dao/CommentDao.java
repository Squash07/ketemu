package id.ketemu.dao;

import id.ketemu.model.Comment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CommentDao extends BaseDao {

    private static final String SELECT = "SELECT cm.*, u.full_name AS author_name FROM comments cm JOIN users u ON u.id = cm.author_id ";

    private static Comment map(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getInt("id"));
        c.setItemId(rs.getInt("item_id"));
        c.setAuthorId(rs.getInt("author_id"));
        c.setAuthorName(rs.getString("author_name"));
        c.setBody(rs.getString("body"));
        c.setCreatedAt(ts(rs, "created_at"));
        c.setUpdatedAt(ts(rs, "updated_at"));
        return c;
    }

    public List<Comment> listByItem(int itemId) {
        return list(SELECT + "WHERE cm.item_id = ? ORDER BY cm.created_at, cm.id", CommentDao::map, itemId);
    }

    public Comment findById(int id) {
        return one(SELECT + "WHERE cm.id = ?", CommentDao::map, id);
    }

    public int create(int itemId, int authorId, String body) {
        return insert("INSERT INTO comments (item_id, author_id, body) VALUES (?, ?, ?)", itemId, authorId, body);
    }

    public void update(int id, String body) {
        update("UPDATE comments SET body = ?, updated_at = now() WHERE id = ?", body, id);
    }

    public void delete(int id) {
        update("DELETE FROM comments WHERE id = ?", id);
    }
}
