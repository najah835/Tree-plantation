package treeplantation.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** JDBC helper: opens connections and runs parameterised queries. */
public final class Db {
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
            if (in != null) CONFIG.load(in);
        } catch (IOException ignored) {
            // defaults below are used
        }
    }

    private Db() {}

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("url", "jdbc:mysql://localhost:3306/tree_plantation"),
                CONFIG.getProperty("user", "root"),
                CONFIG.getProperty("password", ""));
    }

    /** Runs a SELECT and returns every row as an Object[] (column order = SELECT order). */
    public static List<Object[]> query(String sql, Object... params) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 0; i < cols; i++) row[i] = rs.getObject(i + 1);
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    /** Runs INSERT / UPDATE / DELETE and returns the number of affected rows. */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) ps.setNull(i + 1, Types.NULL);
            else ps.setObject(i + 1, params[i]);
        }
    }
}
