package treeplantation.dao;

import treeplantation.db.Db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class LocationDao {
    private LocationDao() {}

    /** Columns: id, name, area, place_type */
    public static List<Object[]> search(String text) throws SQLException {
        String like = "%" + text + "%";
        return Db.query("SELECT location_id, name, area, place_type FROM location "
                + "WHERE name LIKE ? OR area LIKE ? OR place_type LIKE ? ORDER BY name", like, like, like);
    }

    public static void insert(String name, String area, String type) throws SQLException {
        Db.update("INSERT INTO location (name, area, place_type) VALUES (?, ?, ?)", name, area, type);
    }

    public static void update(int id, String name, String area, String type) throws SQLException {
        Db.update("UPDATE location SET name = ?, area = ?, place_type = ? WHERE location_id = ?",
                name, area, type, id);
    }

    public static void delete(int id) throws SQLException {
        Db.update("DELETE FROM location WHERE location_id = ?", id);
    }

    public static List<Item> items() throws SQLException {
        List<Item> list = new ArrayList<>();
        for (Object[] r : Db.query("SELECT location_id, name FROM location ORDER BY name")) {
            list.add(new Item(((Number) r[0]).intValue(), String.valueOf(r[1])));
        }
        return list;
    }
}
