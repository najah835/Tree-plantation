package treeplantation.ui;

import treeplantation.dao.Item;
import treeplantation.dao.LocationDao;
import treeplantation.dao.PlantationDao;
import treeplantation.dao.SpeciesDao;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Search and View: filter plantations by species, location, date range and condition. */
final class SearchPanel extends JPanel implements Refreshable {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Date", "Species", "Location", "Planted by", "Trees", "Condition"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            Object v = getRowCount() > 0 ? getValueAt(0, c) : null;
            return v == null ? Object.class : v.getClass();
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<Item> species = new JComboBox<>();
    private final JComboBox<Item> location = new JComboBox<>();
    private final JComboBox<String> condition =
            new JComboBox<>(new String[]{"All", "Healthy", "Weak", "Damaged", "Dead"});
    private final JTextField from = new JTextField();
    private final JTextField to = new JTextField();
    private final JLabel summary = Ui.label(" ", Theme.BODY, Theme.MUTED);
    private final JLabel emptyHint = Ui.label(" ", Theme.BODY, Theme.MUTED);
    private boolean loading;

    SearchPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);
        texts.add(Ui.label("Search and view", Theme.TITLE, Theme.INK));
        texts.add(Box.createVerticalStrut(2));
        texts.add(Ui.label("Filter plantation records by species, location, date or condition.", Theme.BODY, Theme.MUTED));

        // Filter card
        JPanel filters = Theme.card(new GridBagLayout());
        String[] names = {"Species", "Location", "Condition", "Planted from", "Planted to"};
        JComponent5 comps = new JComponent5(species, location, condition, from, to);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        for (int i = 0; i < 5; i++) {
            gc.gridx = i;
            gc.gridy = 0;
            gc.insets = new Insets(0, 0, 4, 10);
            filters.add(Ui.label(names[i], Theme.BOLD.deriveFont(13f), Theme.INK), gc);
            gc.gridy = 1;
            comps.get(i).setPreferredSize(new Dimension(120, 34));
            filters.add(comps.get(i), gc);
        }
        from.putClientProperty("JTextField.placeholderText", "yyyy-mm-dd");
        to.putClientProperty("JTextField.placeholderText", "yyyy-mm-dd");

        AppButton searchBtn = new AppButton("Search", AppButton.Kind.PRIMARY);
        AppButton clearBtn = new AppButton("Clear filters", AppButton.Kind.SECONDARY);
        searchBtn.addActionListener(e -> run());
        clearBtn.addActionListener(e -> clear());
        from.addActionListener(e -> run());
        to.addActionListener(e -> run());
        species.addActionListener(e -> { if (!loading) run(); });
        location.addActionListener(e -> { if (!loading) run(); });
        condition.addActionListener(e -> { if (!loading) run(); });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(searchBtn);
        buttons.add(clearBtn);
        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 5;
        gc.insets = new Insets(10, 0, 0, 0);
        filters.add(buttons, gc);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(texts, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // Results
        Theme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(5).setCellRenderer(new ConditionRenderer());
        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.LINE));
        tableBox.add(Ui.scroll(table), BorderLayout.CENTER);

        JPanel results = new JPanel(new BorderLayout(0, 8));
        results.setOpaque(false);
        JPanel sumRow = new JPanel(new BorderLayout());
        sumRow.setOpaque(false);
        sumRow.add(summary, BorderLayout.WEST);
        sumRow.add(emptyHint, BorderLayout.EAST);
        results.add(sumRow, BorderLayout.NORTH);
        results.add(tableBox, BorderLayout.CENTER);
        add(results, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        loading = true;
        try {
            List<Item> s = new ArrayList<>();
            s.add(new Item(0, "All species"));
            s.addAll(SpeciesDao.items());
            Ui.fill(species, s);
            List<Item> l = new ArrayList<>();
            l.add(new Item(0, "All locations"));
            l.addAll(LocationDao.items());
            Ui.fill(location, l);
        } catch (SQLException e) {
            Ui.error(this, e);
        } finally {
            loading = false;
        }
        run();
    }

    private void clear() {
        loading = true;
        species.setSelectedIndex(0);
        location.setSelectedIndex(0);
        condition.setSelectedIndex(0);
        from.setText("");
        to.setText("");
        loading = false;
        run();
    }

    private void run() {
        try {
            LocalDate fromDate = Ui.parseOptionalDate(from.getText(), "start date");
            LocalDate toDate = Ui.parseOptionalDate(to.getText(), "end date");
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                throw new ValidationException("The start date must be before the end date.");
            }
            Item s = (Item) species.getSelectedItem();
            Item l = (Item) location.getSelectedItem();
            String cond = (String) condition.getSelectedItem();
            List<Object[]> rows = PlantationDao.search(
                    s == null ? 0 : s.id(), l == null ? 0 : l.id(),
                    "All".equals(cond) ? null : cond, fromDate, toDate);

            model.setRowCount(0);
            long trees = 0;
            for (Object[] r : rows) {
                model.addRow(new Object[]{Ui.str(r[1]), r[2], r[3], r[4], r[5], r[6]});
                trees += Ui.toInt(r[5]);
            }
            summary.setText(rows.size() + (rows.size() == 1 ? " record" : " records") + " · " + trees + " trees");
            emptyHint.setText(rows.isEmpty() ? "No plantation matches these filters. Clear a filter to see more." : " ");
        } catch (ValidationException e) {
            Ui.warn(this, e.getMessage());
        } catch (SQLException e) {
            Ui.error(this, e);
        }
    }

    /** Tiny helper so the five filter inputs can be laid out in a loop. */
    private static final class JComponent5 {
        private final javax.swing.JComponent[] items;

        JComponent5(javax.swing.JComponent... items) {
            this.items = items;
        }

        javax.swing.JComponent get(int i) {
            return items[i];
        }
    }
}
