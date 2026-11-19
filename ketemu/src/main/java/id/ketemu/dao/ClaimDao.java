package id.ketemu.dao;

import id.ketemu.model.Claim;
import id.ketemu.util.Icons;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClaimDao extends BaseDao {

    private static final String SELECT =
            "SELECT cl.*, i.code AS item_code, i.title AS item_title, i.kind AS item_kind, i.status AS item_status, "
            + "c.icon AS category_icon, i.reporter_id AS finder_id, f.full_name AS finder_name, f.email AS finder_email, "
            + "f.phone AS finder_phone, i.held_at_id, h.name AS held_at_name, u.full_name AS claimant_name, "
            + "u.email AS claimant_email, u.phone AS claimant_phone, m.name AS meet_location_name, "
            + "(SELECT count(*) FROM claims c2 WHERE c2.item_id = cl.item_id AND c2.id <= cl.id) AS seq "
            + "FROM claims cl "
            + "JOIN items i ON i.id = cl.item_id "
            + "JOIN categories c ON c.id = i.category_id "
            + "JOIN users f ON f.id = i.reporter_id "
            + "JOIN users u ON u.id = cl.claimant_id "
            + "LEFT JOIN locations m ON m.id = cl.meet_location_id "
            + "LEFT JOIN locations h ON h.id = i.held_at_id ";

    private static Claim map(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setId(rs.getInt("id"));
        c.setItemId(rs.getInt("item_id"));
        c.setItemCode(rs.getString("item_code"));
        c.setItemTitle(rs.getString("item_title"));
        c.setItemKind(rs.getString("item_kind"));
        c.setItemStatus(rs.getString("item_status"));
        c.setItemIcon(Icons.guess(rs.getString("item_title"), rs.getString("category_icon")));
        c.setFinderId(rs.getInt("finder_id"));
        c.setFinderName(rs.getString("finder_name"));
        c.setFinderEmail(rs.getString("finder_email"));
        c.setFinderPhone(rs.getString("finder_phone"));
        c.setHeldAtId(intOrNull(rs, "held_at_id"));
        c.setHeldAtName(rs.getString("held_at_name"));
        c.setClaimantId(rs.getInt("claimant_id"));
        c.setClaimantName(rs.getString("claimant_name"));
        c.setClaimantEmail(rs.getString("claimant_email"));
        c.setClaimantPhone(rs.getString("claimant_phone"));
        c.setProof(rs.getString("proof"));
        c.setMeetLocationId(intOrNull(rs, "meet_location_id"));
        c.setMeetLocationName(rs.getString("meet_location_name"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(ts(rs, "created_at"));
        c.setDecidedAt(ts(rs, "decided_at"));
        c.setSeq(rs.getInt("seq"));
        return c;
    }

    public Claim findById(int id) {
        return one(SELECT + "WHERE cl.id = ?", ClaimDao::map, id);
    }

    public List<Claim> listByClaimant(int userId) {
        return list(SELECT + "WHERE cl.claimant_id = ? "
                + "ORDER BY CASE cl.status WHEN 'approved' THEN 0 WHEN 'pending' THEN 1 ELSE 2 END, cl.created_at DESC", ClaimDao::map, userId);
    }

    public List<Claim> listByItem(int itemId) {
        return list(SELECT + "WHERE cl.item_id = ? ORDER BY cl.id", ClaimDao::map, itemId);
    }

    public Claim activeFor(int itemId, int userId) {
        return one(SELECT + "WHERE cl.item_id = ? AND cl.claimant_id = ? AND cl.status IN ('pending','approved')",
                ClaimDao::map, itemId, userId);
    }

    public int create(int itemId, int claimantId, String proof, Integer meetLocationId) {
        return insert("INSERT INTO claims (item_id, claimant_id, proof, meet_location_id) VALUES (?, ?, ?, ?)",
                itemId, claimantId, proof, meetLocationId);
    }

    public void updateProof(int id, String proof, Integer meetLocationId) {
        update("UPDATE claims SET proof = ?, meet_location_id = ? WHERE id = ?", proof, meetLocationId, id);
    }

    public void setStatus(int id, String status) {
        update("UPDATE claims SET status = ?, decided_at = now() WHERE id = ?", status, id);
    }

    /** Rejects every other pending claim on the item and returns the claimant ids that were affected. */
    public List<Integer> rejectOthers(int itemId, int exceptClaimId) {
        return list("UPDATE claims SET status = 'rejected', decided_at = now() "
                + "WHERE item_id = ? AND id <> ? AND status = 'pending' RETURNING claimant_id", rs -> rs.getInt(1), itemId, exceptClaimId);
    }

    public Claim approvedFor(int itemId) {
        return one(SELECT + "WHERE cl.item_id = ? AND cl.status = 'approved' ORDER BY cl.id DESC LIMIT 1", ClaimDao::map, itemId);
    }

    public long countPending() {
        return count("SELECT count(*) FROM claims WHERE status = 'pending'");
    }
}
