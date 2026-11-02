package treeplantation.ui;

import treeplantation.dao.Item;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

/** Small helpers used by many screens. */
final class Ui {
    private Ui() {}

    static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        if (c instanceof javax.swing.JTable) {
            sp.setColumnHeaderView(((javax.swing.JTable) c).getTableHeader());
        }
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.PANEL);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Check your input", JOptionPane.WARNING_MESSAGE);
    }

    static void error(Component parent, Exception e) {
        String msg = e.getMessage();
        if (e instanceof SQLException) {
            msg = "The database reported a problem:\n" + msg
                    + "\n\nCheck that MySQL is running and that db.properties is correct.";
        }
        JOptionPane.showMessageDialog(parent, msg, "Database error", JOptionPane.ERROR_MESSAGE);
    }

    /** Replaces the items of a combo box and keeps the previous selection when possible. */
    static void fill(JComboBox<Item> box, List<Item> items) {
        Object previous = box.getSelectedItem();
        DefaultComboBoxModel<Item> model = new DefaultComboBoxModel<>();
        for (Item i : items) model.addElement(i);
        box.setModel(model);
        if (previous instanceof Item) select(box, ((Item) previous).id());
        else if (model.getSize() > 0) box.setSelectedIndex(0);
    }

    static void select(JComboBox<Item> box, int id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).id() == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    static int toInt(Object o) {
        return ((Number) o).intValue();
    }

    static LocalDate toLocalDate(Object o) {
        if (o == null) return LocalDate.now();
        if (o instanceof LocalDate) return (LocalDate) o;
        if (o instanceof Date) return new java.sql.Date(((Date) o).getTime()).toLocalDate();
        return LocalDate.parse(o.toString());
    }

    static Date toUtilDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Blank text gives null (no filter). Otherwise the text must look like 2026-10-31. */
    static LocalDate parseOptionalDate(String text, String fieldName) throws ValidationException {
        String s = text == null ? "" : text.trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Enter the " + fieldName + " as yyyy-mm-dd, for example 2026-10-31.");
        }
    }
}
