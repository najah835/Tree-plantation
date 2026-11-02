package treeplantation.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/** A flat rounded button that looks the same on every Swing look and feel. */
final class AppButton extends JButton {
    enum Kind { PRIMARY, SECONDARY, DANGER, NAV }

    private final Kind kind;
    private boolean selected;

    AppButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(kind == Kind.NAV ? Theme.BODY : Theme.BOLD);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int v = kind == Kind.NAV ? 10 : 8;
        setBorder(BorderFactory.createEmptyBorder(v, 16, v, 16));
        if (kind == Kind.NAV) setHorizontalAlignment(SwingConstants.LEFT);
        applyStyle();
    }

    void setNavSelected(boolean value) {
        selected = value;
        applyStyle();
        repaint();
    }

    private void applyStyle() {
        switch (kind) {
            case PRIMARY:   setForeground(Color.WHITE); break;
            case DANGER:    setForeground(Theme.DANGER); break;
            case NAV:
                setForeground(selected ? Theme.SIDEBAR : Theme.SIDE_TEXT);
                setFont(selected ? Theme.BOLD : Theme.BODY);
                break;
            default:        setForeground(Theme.INK);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hot = isEnabled() && (getModel().isRollover() || getModel().isArmed());
        Color fill;
        Color line = null;
        switch (kind) {
            case PRIMARY:
                fill = hot ? Theme.ACCENT_HOVER : Theme.ACCENT;
                break;
            case SECONDARY:
                fill = hot ? Theme.PANEL2 : Theme.PANEL;
                line = Theme.LINE;
                break;
            case DANGER:
                fill = hot ? new Color(0xFBE9E6) : Theme.PANEL;
                line = Theme.DANGER;
                break;
            default:
                fill = selected ? Theme.SIDE_ACTIVE : (hot ? new Color(255, 255, 255, 28) : null);
        }
        if (!isEnabled()) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        Shape shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        if (fill != null) {
            g2.setColor(fill);
            g2.fill(shape);
        }
        if (line != null) {
            g2.setColor(line);
            g2.draw(shape);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
