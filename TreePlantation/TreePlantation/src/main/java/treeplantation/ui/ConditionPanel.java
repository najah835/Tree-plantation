package treeplantation.ui;

import treeplantation.dao.PlantationDao;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Tree Condition Monitoring: update Healthy / Weak / Damaged / Dead after a field check. */
final class ConditionPanel extends JPanel implements Refreshable {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Species", "Location", "Trees", "Condition", "Last checked", "Field note"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            Object v = getRowCount() > 0 ? getValueAt(0, c) : null;
            return v == null ? Object.class : v.getClass();
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> filter =
            new JComboBox<>(new String[]{"All", "Healthy", "Weak", "Damaged", "Dead"});
    private final JComboBox<String> newCondition = new JComboBox<>(Theme.CONDITIONS);
    private final JTextField note = new JTextField();
    private final JLabel selectedLabel =
            Ui.label("Select a plantation in the table to update its condition.", Theme.BODY, Theme.MUTED);
    private final JLabel feedback = Ui.label(" ", Theme.SMALL, Theme.ACCENT);
    private final AppButton updateBtn = new AppButton("Update condition", AppButton.Kind.PRIMARY);

    /** Raw rows: 0 id, 1 species, 2 location, 3 trees, 4 condition, 5 last checked, 6 note. */
    private List<Object[]> rows = new ArrayList<>();
    private Integer selectedId;
    private boolean adjusting;

    ConditionPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);
        texts.add(Ui.label("Condition monitoring", Theme.TITLE, Theme.INK));
        texts.add(Box.createVerticalStrut(2));
        texts.add(Ui.label("Update the condition of planted trees after each field check.", Theme.BODY, Theme.MUTED));
        add(texts, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(3).setCellRenderer(new ConditionRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || adjusting) return;
            int view = table.getSelectedRow();
            if (view < 0) return;
            select(rows.get(table.convertRowIndexToModel(view)));
        });

        filter.setPreferredSize(new Dimension(150, 32));
        filter.addActionListener(e -> reload());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 10, 0));
        bar.add(Ui.label("Show", Theme.BOLD, Theme.INK));
        bar.add(filter);

        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.LINE));
        tableBox.add(Ui.scroll(table), BorderLayout.CENTER);
        JPanel middle = new JPanel(new BorderLayout());
        middle.setOpaque(false);
        middle.add(bar, BorderLayout.NORTH);
        middle.add(tableBox, BorderLayout.CENTER);
        add(middle, BorderLayout.CENTER);

        // Update card
        JPanel card = Theme.card(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 8, 12);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 3; gc.weightx = 1;
        card.add(selectedLabel, gc);

        gc.gridwidth = 1; gc.gridy = 1; gc.weightx = 0;
        gc.gridx = 0; card.add(Ui.label("New condition", Theme.BOLD.deriveFont(13f), Theme.INK), gc);
        gc.gridx = 1; gc.weightx = 1; card.add(Ui.label("Field note", Theme.BOLD.deriveFont(13f), Theme.INK), gc);
        gc.gridx = 2; gc.weightx = 0; card.add(new JLabel(" "), gc);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 0, 12);
        newCondition.setPreferredSize(new Dimension(160, 34));
        note.setPreferredSize(new Dimension(100, 34));
        gc.gridx = 0; gc.weightx = 0; card.add(newCondition, gc);
        gc.gridx = 1; gc.weightx = 1; card.add(note, gc);
        gc.gridx = 2; gc.weightx = 0; card.add(updateBtn, gc);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 3; gc.insets = new Insets(8, 0, 0, 0);
        card.add(feedback, gc);

        updateBtn.setEnabled(false);
        updateBtn.addActionListener(e -> onUpdate());
        add(card, BorderLayout.SOUTH);
    }

    private void select(Object[] r) {
        selectedId = Ui.toInt(r[0]);
        selectedLabel.setText(r[1] + " at " + r[2] + " · " + r[3] + " trees");
        selectedLabel.setForeground(Theme.INK);
        newCondition.setSelectedItem(Ui.str(r[4]));
        note.setText(Ui.str(r[6]));
        updateBtn.setEnabled(true);
        feedback.setText(" ");
    }

    @Override
    public void refresh() {
        reload();
    }

    private void reload() {
        String cond = (String) filter.getSelectedItem();
        try {
            rows = PlantationDao.monitorList("All".equals(cond) ? null : cond);
        } catch (SQLException e) {
            Ui.error(this, e);
            return;
        }
        adjusting = true;
        model.setRowCount(0);
        for (Object[] r : rows) {
            model.addRow(new Object[]{r[1], r[2], r[3], r[4],
                    r[5] == null ? "Never" : r[5].toString(), Ui.str(r[6])});
        }
        adjusting = false;
        if (selectedId != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (Ui.toInt(rows.get(i)[0]) == selectedId) {
                    int v = table.convertRowIndexToView(i);
                    adjusting = true;
                    table.setRowSelectionInterval(v, v);
                    adjusting = false;
                }
            }
        }
    }

    private void onUpdate() {
        if (selectedId == null) return;
        try {
            PlantationDao.updateCondition(selectedId, (String) newCondition.getSelectedItem(), note.getText().trim());
            reload();
            feedback.setText("Condition updated. Last checked is now today.");
        } catch (SQLException e) {
            Ui.error(this, e);
        }
    }
}
