package treeplantation.ui;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/** Stacked bar plus legend showing how many trees are Healthy / Weak / Damaged / Dead. */
final class ConditionChart extends JPanel {
    private final int[] counts = new int[4];

    ConditionChart() {
        setOpaque(false);
    }

    void setData(Map<String, Integer> byCondition) {
        for (int i = 0; i < 4; i++) counts[i] = byCondition.getOrDefault(Theme.CONDITIONS[i], 0);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 190);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int total = 0;
        for (int c : counts) total += c;
        int w = getWidth();

        // Stacked bar
        g2.setClip(new RoundRectangle2D.Float(0, 4, w, 28, 10, 10));
        g2.setColor(Theme.PANEL2);
        g2.fillRect(0, 4, w, 28);
        if (total > 0) {
            int x = 0;
            for (int i = 0; i < 4; i++) {
                int seg = (i == 3) ? w - x : (int) Math.round((double) w * counts[i] / total);
                g2.setColor(Theme.conditionColor(Theme.CONDITIONS[i]));
                g2.fillRect(x, 4, seg, 28);
                x += seg;
            }
        }
        g2.setClip(null);

        // Legend (2 x 2)
        Font bold = Theme.BOLD;
        Font small = Theme.SMALL;
        for (int i = 0; i < 4; i++) {
            int col = i % 2, row = i / 2;
            int x = col * (w / 2);
            int y = 56 + row * 52;
            String name = Theme.CONDITIONS[i];
            g2.setColor(Theme.conditionColor(name));
            g2.fillOval(x, y + 3, 11, 11);
            g2.setFont(bold);
            g2.setColor(Theme.INK);
            g2.drawString(name, x + 18, y + 14);
            int pct = total == 0 ? 0 : (int) Math.round(100.0 * counts[i] / total);
            g2.setFont(small);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Theme.MUTED);
            g2.drawString(counts[i] + " trees (" + pct + "%)", x + 18, y + 14 + fm.getHeight() + 2);
        }
        g2.dispose();
    }
}
