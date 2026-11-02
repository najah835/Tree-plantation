package treeplantation.dao;

import treeplantation.db.Db;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class PlantationDao {
    private PlantationDao() {}

    /**
     * Columns: 0 id, 1 date, 2 species, 3 location, 4 planter, 5 tree_count, 6 condition,
     * 7 species_id, 8 location_id, 9 planter_id
     */
    private static final String BASE =
            "SELECT p.plantation_id, p.plantation_date, s.common_name, l.name, pl.name, "
          + "p.tree_count, p.tree_condition, p.species_id, p.location_id, p.planter_id "
          + "FROM plantation p "
          + "JOIN species s ON s.species_id = p.species_id "
          + "JOIN location l ON l.location_id = p.location_id "
          + "JOIN planter pl ON pl.planter_id = p.planter_id ";

    private static final String ORDER = "ORDER BY p.plantation_date DESC, p.plantation_id DESC";

    public static List<Object[]> list(String text) throws SQLException {
        String like = "%" + text + "%";
        return Db.query(BASE + "WHERE s.common_name LIKE ? OR l.name LIKE ? OR pl.name LIKE ? "
                + "OR p.tree_condition LIKE ? " + ORDER, like, like, like, like);
    }

    /** Filters for the Search and view screen. Use 0 / null for "no filter". */
    public static List<Object[]> search(int speciesId, int locationId, String condition,
                                        LocalDate from, LocalDate to) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (speciesId > 0)  { sql.append("AND p.species_id = ? ");       params.add(speciesId); }
        if (locationId > 0) { sql.append("AND p.location_id = ? ");      params.add(locationId); }
        if (condition != null) { sql.append("AND p.tree_condition = ? "); params.add(condition); }
        if (from != null)   { sql.append("AND p.plantation_date >= ? "); params.add(from); }
        if (to != null)     { sql.append("AND p.plantation_date <= ? "); params.add(to); }
        sql.append(ORDER);
        return Db.query(sql.toString(), params.toArray());
    }

    public static void insert(int speciesId, int locationId, int planterId, LocalDate date, int count)
            throws SQLException {
        Db.update("INSERT INTO plantation (species_id, location_id, planter_id, plantation_date, tree_count) "
                + "VALUES (?, ?, ?, ?, ?)", speciesId, locationId, planterId, date, count);
    }

    public static void update(int id, int speciesId, int locationId, int planterId, LocalDate date, int count)
            throws SQLException {
        Db.update("UPDATE plantation SET species_id = ?, location_id = ?, planter_id = ?, "
                + "plantation_date = ?, tree_count = ? WHERE plantation_id = ?",
                speciesId, locationId, planterId, date, count, id);
    }

    public static void delete(int id) throws SQLException {
        Db.update("DELETE FROM plantation WHERE plantation_id = ?", id);
    }

    // ---- Condition monitoring ----------------------------------------------------------

    /** Columns: id, species, location, tree_count, condition, last_checked, note */
    public static List<Object[]> monitorList(String condition) throws SQLException {
        String sql = "SELECT p.plantation_id, s.common_name, l.name, p.tree_count, p.tree_condition, "
                + "p.last_checked, p.note FROM plantation p "
                + "JOIN species s ON s.species_id = p.species_id "
                + "JOIN location l ON l.location_id = p.location_id ";
        if (condition == null) return Db.query(sql + ORDER);
        return Db.query(sql + "WHERE p.tree_condition = ? " + ORDER, condition);
    }

    public static void updateCondition(int id, String condition, String note) throws SQLException {
        Db.update("UPDATE plantation SET tree_condition = ?, note = ?, last_checked = CURDATE() "
                + "WHERE plantation_id = ?", condition, note, id);
    }
}
