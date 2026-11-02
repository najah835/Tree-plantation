package treeplantation.dao;

import treeplantation.db.Db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** People and organizations that plant trees. */
public final class PlanterDao {
    private PlanterDao() {}

    /** Columns: id, name, kind, contact */
    public static List<Object[]> search(String text) throws SQLException {
        String like = "%" + text + "%";
        return Db.query("SELECT planter_id, name, kind, contact FROM planter "
                + "WHERE name LIKE ? OR kind LIKE ? OR contact LIKE ? ORDER BY name", like, like, like);
    }

    public static void insert(String name, String kind, String contact) throws SQLException {
        Db.update("INSERT INTO planter (name, kind, contact) VALUES (?, ?, ?)", name, kind, contact);
    }

    public static void update(int id, String name, String kind, String contact) throws SQLException {
        Db.update("UPDATE planter SET name = ?, kind = ?, contact = ? WHERE planter_id = ?",
                name, kind, contact, id);
    }

    public static void delete(int id) throws SQLException {
        Db.update("DELETE FROM planter WHERE planter_id = ?", id);
    }

    public static List<Item> items() throws SQLException {
        List<Item> list = new ArrayList<>();
        for (Object[] r : Db.query("SELECT planter_id, name FROM planter ORDER BY name")) {
            list.add(new Item(((Number) r[0]).intValue(), String.valueOf(r[1])));
        }
        return list;
    }
}
