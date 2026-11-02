package treeplantation.ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

/** Colours, fonts and small styling helpers shared by every screen. */
final class Theme {
    private Theme() {}

    static final Color SIDEBAR     = new Color(0x14301F);
    static final Color SIDE_TEXT   = new Color(0xD8E6DC);
    static final Color SIDE_ACTIVE = new Color(0xF0C24B);
    static final Color ACCENT      = new Color(0x1F6B3A);
    static final Color ACCENT_HOVER = new Color(0x27824A);
    static final Color BG          = new Color(0xE9EEEA);
    static final Color PANEL       = Color.WHITE;
    static final Color PANEL2      = new Color(0xF4F7F5);
    static final Color LINE        = new Color(0xD3DCD6);
    static final Color INK         = new Color(0x17231C);
    static final Color MUTED       = new Color(0x5B6B61);
    static final Color DANGER      = new Color(0xB3372B);
    static final Color SELECT      = new Color(0xE0F1E5);

    static final Font BODY  = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    static final Font BOLD  = BODY.deriveFont(Font.BOLD);
    static final Font SMALL = BODY.deriveFont(12.5f);
    static final Font TITLE = BODY.deriveFont(Font.BOLD, 24f);
    static final Font H3    = BODY.deriveFont(Font.BOLD, 16f);
    static final Font KPI   = BODY.deriveFont(Font.BOLD, 28f);

    static final String[] CONDITIONS = {"Healthy", "Weak", "Damaged", "Dead"};

    static Color conditionColor(String c) {
        switch (c == null ? "" : c) {
            case "Healthy": return new Color(0x2E8B4F);
            case "Weak":    return new Color(0xB07F00);
            case "Damaged": return new Color(0xC2551A);
            case "Dead":    return new Color(0x6E645C);
            default:        return MUTED;
        }
    }

    static Color conditionBg(String c) {
        switch (c == null ? "" : c) {
            case "Healthy": return new Color(0xE0F1E5);
            case "Weak":    return new Color(0xFAF0CF);
            case "Damaged": return new Color(0xFBE3D3);
            case "Dead":    return new Color(0xEBE7E3);
            default:        return PANEL2;
        }
    }

    /** White rounded-border container with padding. */
    static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(LINE, 1, true), new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    static void styleTable(JTable t) {
        t.setRowHeight(32);
        t.setFont(BODY);
        t.setForeground(INK);
        t.setBackground(PANEL);
        t.setGridColor(LINE);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(INK);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setFont(BOLD.deriveFont(13f));
        h.setBackground(PANEL2);
        h.setForeground(MUTED);
        h.setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer text = new DefaultTableCellRenderer();
        text.setBorder(new EmptyBorder(0, 10, 0, 10));
        t.setDefaultRenderer(Object.class, text);

        DefaultTableCellRenderer number = new DefaultTableCellRenderer();
        number.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        number.setBorder(new EmptyBorder(0, 10, 0, 14));
        t.setDefaultRenderer(Number.class, number);
    }
}
