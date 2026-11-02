package treeplantation.dao;

import treeplantation.db.Db;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Queries behind the dashboard (Reports and Statistics). */
public final class ReportDao {
    private ReportDao() {}

    /** Returns {total trees, plantation records, species used, locations used}. */
    public static long[] totals() throws SQLException {
        Object[] r = Db.query("SELECT COALESCE(SUM(tree_count), 0), COUNT(*), "
                + "COUNT(DISTINCT species_id), COUNT(DISTINCT location_id) FROM plantation").get(0);
        return new long[]{((Number) r[0]).longValue(), ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue(), ((Number) r[3]).longValue()};
    }

    public static Map<String, Integer> treesBySpecies() throws SQLException {
        return toMap(Db.query("SELECT s.common_name, SUM(p.tree_count) FROM plantation p "
                + "JOIN species s ON s.species_id = p.species_id "
                + "GROUP BY s.species_id, s.common_name ORDER BY SUM(p.tree_count) DESC"));
    }

    public static Map<String, Integer> treesByLocation() throws SQLException {
        return toMap(Db.query("SELECT l.name, SUM(p.tree_count) FROM plantation p "
                + "JOIN location l ON l.location_id = p.location_id "
                + "GROUP BY l.location_id, l.name ORDER BY SUM(p.tree_count) DESC"));
    }

    public static Map<String, Integer> treesByCondition() throws SQLException {
        return toMap(Db.query("SELECT tree_condition, SUM(tree_count) FROM plantation GROUP BY tree_condition"));
    }

    private static Map<String, Integer> toMap(List<Object[]> rows) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Object[] r : rows) map.put(String.valueOf(r[0]), ((Number) r[1]).intValue());
        return map;
    }
}
