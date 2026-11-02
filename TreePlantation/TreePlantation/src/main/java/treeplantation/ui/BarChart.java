package treeplantation.ui;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/** Horizontal bar chart drawn with Java2D (top 6 entries). */
final class BarChart extends JPanel {
    private Map<String, Integer> data = new LinkedHashMap<>();

    BarChart() {
        setOpaque(false);
    }

    void setData(Map<String, Integer> newData) {
        data = new LinkedHashMap<>();
        int n = 0;
        for (Map.Entry<String, Integer> e : newData.entrySet()) {
            if (n++ == 6) break;
            data.put(e.getKey(), e.getValue());
        }
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
        g2.setFont(Theme.BODY);
        FontMetrics fm = g2.getFontMetrics();

        if (data.isEmpty()) {
            g2.setColor(Theme.MUTED);
            g2.drawString("No plantation records yet.", 0, 20);
            g2.dispose();
            return;
        }
        int max = 1;
        for (int v : data.values()) max = Math.max(max, v);

        int labelW = 140, valueW = 48, gap = 10, rowH = 30;
        int trackX = labelW + gap;
        int trackW = Math.max(40, getWidth() - trackX - valueW - gap);
        int y = 4;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            int base = y + (rowH + fm.getAscent() - fm.getDescent()) / 2 - 2;
            g2.setColor(Theme.INK);
            g2.drawString(clip(e.getKey(), fm, labelW), 0, base);

            g2.setColor(Theme.PANEL2);
            g2.fillRoundRect(trackX, y + 7, trackW, 14, 6, 6);
            g2.setColor(Theme.ACCENT);
            g2.fillRoundRect(trackX, y + 7, Math.max(6, (int) ((long) trackW * e.getValue() / max)), 14, 6, 6);

            String v = String.valueOf(e.getValue());
            g2.setColor(Theme.INK);
            g2.drawString(v, getWidth() - fm.stringWidth(v), base);
            y += rowH;
        }
        g2.dispose();
    }

    private static String clip(String s, FontMetrics fm, int width) {
        if (fm.stringWidth(s) <= width) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > width) t = t.substring(0, t.length() - 1);
        return t + "…";
    }
}
