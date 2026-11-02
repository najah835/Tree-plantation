package treeplantation.ui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base screen for add / edit / delete / search on one table:
 * a searchable table on the left and a form on the right.
 * Subclasses add their form fields in the constructor with {@link #addField}.
 */
abstract class CrudPanel extends JPanel implements Refreshable {
    protected final DefaultTableModel model;
    protected final JTable table;
    /** Raw rows from the database; column 0 is always the record id. */
    protected List<Object[]> rows = new ArrayList<>();
    protected Integer selectedId;

    private final String noun;
    private final JTextField searchField = new JTextField();
    private final JPanel formBody = new JPanel(new GridBagLayout());
    private final JLabel formTitle = Ui.label("", Theme.H3, Theme.INK);
    private final JLabel feedback = Ui.label(" ", Theme.SMALL, Theme.ACCENT);
    private final JLabel countLabel = Ui.label(" ", Theme.SMALL, Theme.MUTED);
    private final AppButton saveBtn = new AppButton("Save", AppButton.Kind.PRIMARY);
    private final AppButton deleteBtn = new AppButton("Delete", AppButton.Kind.DANGER);
    private int formRow;
    private boolean adjusting;

    CrudPanel(String title, String subtitle, String noun, String[] columns) {
        super(new BorderLayout(0, 16));
        this.noun = noun;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        // Header: title on the left, "New" button on the right
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);
        texts.add(Ui.label(title, Theme.TITLE, Theme.INK));
        texts.add(Box.createVerticalStrut(2));
        texts.add(Ui.label(subtitle, Theme.BODY, Theme.MUTED));
        head.add(texts, BorderLayout.WEST);
        AppButton newBtn = new AppButton("New " + noun, AppButton.Kind.PRIMARY);
        newBtn.addActionListener(e -> newRecord());
        JPanel headRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        headRight.setOpaque(false);
        headRight.add(newBtn);
        head.add(headRight, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                Object v = getRowCount() > 0 ? getValueAt(0, column) : null;
                return v == null ? Object.class : v.getClass();
            }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || adjusting) return;
            int view = table.getSelectedRow();
            if (view < 0) return;
            Object[] row = rows.get(table.convertRowIndexToModel(view));
            selectedId = Ui.toInt(row[0]);
            populate(row);
            feedback.setText(" ");
            updateFormMode();
        });

        searchField.putClientProperty("JTextField.placeholderText", "Search " + noun + " records");
        searchField.setFont(Theme.BODY);
        searchField.setPreferredSize(new Dimension(320, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { reload(); }
            @Override public void removeUpdate(DocumentEvent e) { reload(); }
            @Override public void changedUpdate(DocumentEvent e) { reload(); }
        });
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        toolbar.add(searchField, BorderLayout.WEST);
        toolbar.add(countLabel, BorderLayout.EAST);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setOpaque(false);
        tableCard.add(toolbar, BorderLayout.NORTH);
        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.LINE));
        tableBox.add(Ui.scroll(table), BorderLayout.CENTER);
        tableCard.add(tableBox, BorderLayout.CENTER);

        // Form
        JPanel formCard = Theme.card(new BorderLayout(0, 14));
        formCard.setPreferredSize(new Dimension(310, 0));
        formCard.add(formTitle, BorderLayout.NORTH);
        formBody.setOpaque(false);
        JPanel formTop = new JPanel(new BorderLayout());
        formTop.setOpaque(false);
        formTop.add(formBody, BorderLayout.NORTH);
        formCard.add(formTop, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> onSave());
        deleteBtn.addActionListener(e -> onDelete());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(saveBtn);
        buttons.add(deleteBtn);
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(buttons, BorderLayout.NORTH);
        south.add(feedback, BorderLayout.SOUTH);
        formCard.add(south, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(tableCard, BorderLayout.CENTER);
        center.add(formCard, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        updateFormMode();
    }

    // ---- To be provided by each screen -------------------------------------------------

    /** Return rows for the table; column 0 must be the id. */
    protected abstract List<Object[]> fetch(String searchText) throws SQLException;

    /** Put a record's values into the form fields. */
    protected abstract void populate(Object[] row);

    /** Reset the form fields to their empty state. */
    protected abstract void clearInputs();

    /** Insert (id == null) or update (id != null) from the form fields. */
    protected abstract void persist(Integer id) throws SQLException, ValidationException;

    protected abstract void deleteRecord(int id) throws SQLException;

    /** Optional: reload drop-down lists (called every time the screen is shown). */
    protected void reloadLookups() throws SQLException {}

    /** Which columns of the raw row are shown. Default: everything except the id. */
    protected Object[] displayRow(Object[] raw) {
        Object[] shown = Arrays.copyOfRange(raw, 1, raw.length);
        for (int i = 0; i < shown.length; i++) {
            if (shown[i] != null && !(shown[i] instanceof Number)) shown[i] = shown[i].toString();
        }
        return shown;
    }

    // ---- Helpers for subclasses --------------------------------------------------------

    protected final void addField(String label, JComponent field) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;

        gc.gridy = formRow++;
        gc.insets = new Insets(0, 0, 4, 0);
        formBody.add(Ui.label(label, Theme.BOLD.deriveFont(13f), Theme.INK), gc);

        field.setFont(Theme.BODY);
        field.setPreferredSize(new Dimension(100, 34));
        gc.gridy = formRow++;
        gc.insets = new Insets(0, 0, 12, 0);
        formBody.add(field, gc);
    }

    protected final String required(JTextField field, String message) throws ValidationException {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            field.requestFocusInWindow();
            throw new ValidationException(message);
        }
        return text;
    }

    // ---- Behaviour ---------------------------------------------------------------------

    @Override
    public void refresh() {
        try {
            reloadLookups();
        } catch (SQLException e) {
            Ui.error(this, e);
        }
        reload();
    }

    private void reload() {
        try {
            rows = fetch(searchField.getText().trim());
        } catch (SQLException e) {
            Ui.error(this, e);
            return;
        }
        adjusting = true;
        model.setRowCount(0);
        for (Object[] r : rows) model.addRow(displayRow(r));
        adjusting = false;
        countLabel.setText(rows.size() + (rows.size() == 1 ? " record" : " records"));

        if (selectedId != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (Ui.toInt(rows.get(i)[0]) == selectedId) {
                    int view = table.convertRowIndexToView(i);
                    adjusting = true;
                    table.setRowSelectionInterval(view, view);
                    adjusting = false;
                    break;
                }
            }
        }
    }

    private void newRecord() {
        adjusting = true;
        table.clearSelection();
        adjusting = false;
        selectedId = null;
        clearInputs();
        feedback.setText(" ");
        updateFormMode();
    }

    private void updateFormMode() {
        boolean editing = selectedId != null;
        formTitle.setText((editing ? "Edit " : "New ") + noun);
        saveBtn.setText(editing ? "Save changes" : "Add " + noun);
        deleteBtn.setEnabled(editing);
    }

    private void onSave() {
        try {
            boolean isNew = selectedId == null;
            persist(selectedId);
            newRecord();
            reload();
            feedback.setText(isNew ? "Saved." : "Changes saved.");
        } catch (ValidationException e) {
            Ui.warn(this, e.getMessage());
        } catch (SQLException e) {
            Ui.error(this, e);
        }
    }

    private void onDelete() {
        if (selectedId == null) return;
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete this " + noun + "? This cannot be undone.",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            deleteRecord(selectedId);
            newRecord();
            reload();
            feedback.setText("Deleted.");
        } catch (SQLIntegrityConstraintViolationException e) {
            Ui.warn(this, "This " + noun + " is used by plantation records.\n"
                    + "Remove or reassign those plantations first.");
        } catch (SQLException e) {
            Ui.error(this, e);
        }
    }
}
