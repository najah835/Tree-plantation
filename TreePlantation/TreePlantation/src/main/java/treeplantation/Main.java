package treeplantation;

import treeplantation.db.Db;
import treeplantation.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

/** Entry point. Run this class from IntelliJ. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        installLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            try (Connection ignored = Db.connect()) {
                // connection works
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the MySQL database.\n\n" + e.getMessage()
                                + "\n\nCheck that:\n"
                                + " 1. MySQL is running.\n"
                                + " 2. You ran database/schema.sql in MySQL Workbench.\n"
                                + " 3. user and password in src/main/resources/db.properties are correct.",
                        "Database connection failed", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new MainFrame().setVisible(true);
        });
    }

    private static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf"); // from the Maven dependency
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
        } catch (Exception flatLafMissing) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // default look and feel
            }
        }
    }
}
