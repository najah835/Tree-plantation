package treeplantation.ui;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Draws the condition (Healthy / Weak / Damaged / Dead) as a coloured pill with a dot.
 * Everything is painted by hand so it does not depend on any layout manager.
 */
final class ConditionRenderer extends JLabel implements TableCellRenderer {
    private String condition = "";
    private boolean selected;
    private java.awt.Color rowBackground = Theme.PANEL;

    ConditionRenderer() {
        setOpaque(false);
        setFont(Theme.SMALL.deriveFont(java.awt.Font.BOLD));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        condition = value == null ? "" : value.toString();
        selected = isSelected;
        rowBackground = isSelected ? table.getSelectionBackground() : table.getBackground();
        setText("");
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(rowBackground);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (!condition.isEmpty()) {
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int pillH = fm.getHeight() + 6;
            int pillW = 26 + fm.stringWidth(condition);
            int x = 8;
            int y = (getHeight() - pillH) / 2;
            g2.setColor(Theme.conditionBg(condition));
            g2.fillRoundRect(x, y, pillW, pillH, pillH, pillH);
            g2.setColor(Theme.conditionColor(condition));
            g2.fillOval(x + 9, y + pillH / 2 - 4, 8, 8);
            g2.drawString(condition, x + 22, y + (pillH + fm.getAscent() - fm.getDescent()) / 2);
        }
        g2.dispose();
    }
}
