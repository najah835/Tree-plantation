package treeplantation.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

/** The main window: a green sidebar on the left and the selected screen on the right. */
public final class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Refreshable> screens = new LinkedHashMap<>();
    private final Map<String, AppButton> navButtons = new LinkedHashMap<>();

    public MainFrame() {
        super("Tree Plantation and Monitoring Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);
        content.setBackground(Theme.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        register("dashboard", new DashboardPanel());
        register("plantations", new PlantationPanel());
        register("condition", new ConditionPanel());
        register("search", new SearchPanel());
        register("species", new SpeciesPanel());
        register("locations", new LocationPanel());
        register("planters", new PlanterPanel());

        setSize(1320, 820);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        show("dashboard");
    }

    private <T extends JPanel & Refreshable> void register(String key, T panel) {
        screens.put(key, panel);
        content.add(panel, key);
    }

    private void show(String key) {
        navButtons.forEach((k, b) -> b.setNavSelected(k.equals(key)));
        cards.show(content, key);
        screens.get(key).refresh();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setBorder(new EmptyBorder(20, 12, 14, 12));
        side.setPreferredSize(new Dimension(250, 0));

        JLabel brand = Ui.label("<html>Tree Plantation<br>Monitor</html>", Theme.H3.deriveFont(20f), Color.WHITE);
        JLabel tagline = Ui.label("Records and condition tracking", Theme.SMALL, new Color(0x9DB5A4));
        brand.setBorder(new EmptyBorder(0, 10, 4, 0));
        tagline.setBorder(new EmptyBorder(0, 10, 18, 0));
        side.add(left(brand));
        side.add(left(tagline));

        side.add(nav("dashboard", "Dashboard"));
        side.add(section("Records"));
        side.add(nav("plantations", "Plantations"));
        side.add(nav("condition", "Condition monitoring"));
        side.add(nav("search", "Search and view"));
        side.add(section("Master data"));
        side.add(nav("species", "Tree species"));
        side.add(nav("locations", "Locations"));
        side.add(nav("planters", "People and organizations"));

        side.add(Box.createVerticalGlue());
        JLabel db = Ui.label("MySQL · tree_plantation", Theme.SMALL, new Color(0x9DB5A4));
        db.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 40)),
                new EmptyBorder(10, 10, 0, 0)));
        side.add(left(db));
        return side;
    }

    private AppButton nav(String key, String text) {
        AppButton b = new AppButton(text, AppButton.Kind.NAV);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> show(key));
        navButtons.put(key, b);
        return b;
    }

    private JLabel section(String text) {
        JLabel l = Ui.label(text, Theme.SMALL, new Color(0x9DB5A4));
        l.setBorder(new EmptyBorder(16, 10, 6, 0));
        return left(l);
    }

    private static <C extends JLabel> C left(C c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }
}
