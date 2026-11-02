package treeplantation.dao;

import treeplantation.db.Db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class SpeciesDao {
    private SpeciesDao() {}

    /** Columns: id, common_name, scientific_name, species_type */
    public static List<Object[]> search(String text) throws SQLException {
        String like = "%" + text + "%";
        return Db.query("SELECT species_id, common_name, scientific_name, species_type FROM species "
                + "WHERE common_name LIKE ? OR scientific_name LIKE ? OR species_type LIKE ? "
                + "ORDER BY common_name", like, like, like);
    }

    public static void insert(String common, String scientific, String type) throws SQLException {
        Db.update("INSERT INTO species (common_name, scientific_name, species_type) VALUES (?, ?, ?)",
                common, scientific, type);
    }

    public static void update(int id, String common, String scientific, String type) throws SQLException {
        Db.update("UPDATE species SET common_name = ?, scientific_name = ?, species_type = ? WHERE species_id = ?",
                common, scientific, type, id);
    }

    public static void delete(int id) throws SQLException {
        Db.update("DELETE FROM species WHERE species_id = ?", id);
    }

    public static List<Item> items() throws SQLException {
        List<Item> list = new ArrayList<>();
        for (Object[] r : Db.query("SELECT species_id, common_name FROM species ORDER BY common_name")) {
            list.add(new Item(((Number) r[0]).intValue(), String.valueOf(r[1])));
        }
        return list;
    }
}
