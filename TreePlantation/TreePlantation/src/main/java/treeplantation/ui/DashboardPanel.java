package treeplantation.ui;

import treeplantation.dao.PlantationDao;
import treeplantation.dao.ReportDao;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/** Reports and Statistics: totals, trees by species / location, condition split, latest records. */
final class DashboardPanel extends JPanel implements Refreshable {
    private final JLabel totalTrees = Ui.label("0", Theme.KPI, Theme.INK);
    private final JLabel records = Ui.label("0", Theme.KPI, Theme.INK);
    private final JLabel speciesUsed = Ui.label("0", Theme.KPI, Theme.INK);
    private final JLabel locationsUsed = Ui.label("0", Theme.KPI, Theme.INK);
    private final BarChart speciesChart = new BarChart();
    private final BarChart locationChart = new BarChart();
    private final ConditionChart conditionChart = new ConditionChart();
    private final DefaultTableModel latestModel = new DefaultTableModel(
            new String[]{"Date", "Species", "Location", "Trees", "Condition"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            Object v = getRowCount() > 0 ? getValueAt(0, c) : null;
            return v == null ? Object.class : v.getClass();
        }
    };

    DashboardPanel() {
        super(new BorderLayout());
        setBackground(Theme.BG);

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(Theme.BG);
        page.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);
        texts.add(Ui.label("Dashboard", Theme.TITLE, Theme.INK));
        texts.add(Box.createVerticalStrut(2));
        texts.add(Ui.label("Totals and condition statistics across all plantation records.", Theme.BODY, Theme.MUTED));

        JPanel kpis = new JPanel(new GridLayout(1, 4, 14, 0));
        kpis.setOpaque(false);
        kpis.add(kpi("Total trees planted", totalTrees));
        kpis.add(kpi("Plantation records", records));
        kpis.add(kpi("Species used", speciesUsed));
        kpis.add(kpi("Locations planted", locationsUsed));

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0));
        charts.setOpaque(false);
        charts.add(titled("Trees by species", speciesChart));
        charts.add(titled("Trees by location", locationChart));

        JTable latest = new JTable(latestModel);
        Theme.styleTable(latest);
        latest.getColumnModel().getColumn(4).setCellRenderer(new ConditionRenderer());
        JScrollPane latestScroll = Ui.scroll(latest);
        latestScroll.setPreferredSize(new Dimension(300, 200));
        latestScroll.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.LINE));

        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setOpaque(false);
        bottom.add(titled("Tree condition", conditionChart));
        bottom.add(titled("Latest plantations", latestScroll));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTH;
        gc.insets = new Insets(0, 0, 16, 0);
        gc.gridy = 0; page.add(texts, gc);
        gc.gridy = 1; page.add(kpis, gc);
        gc.gridy = 2; page.add(charts, gc);
        gc.gridy = 3; page.add(bottom, gc);
        gc.gridy = 4; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;
        page.add(Box.createGlue(), gc);

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private static JPanel kpi(String caption, JLabel value) {
        JPanel p = Theme.card(new BorderLayout(0, 2));
        p.add(Ui.label(caption, Theme.SMALL, Theme.MUTED), BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private static JPanel titled(String title, JComponent body) {
        JPanel p = Theme.card(new BorderLayout(0, 10));
        p.add(Ui.label(title, Theme.H3, Theme.INK), BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refresh() {
        try {
            long[] t = ReportDao.totals();
            totalTrees.setText(String.valueOf(t[0]));
            records.setText(String.valueOf(t[1]));
            speciesUsed.setText(String.valueOf(t[2]));
            locationsUsed.setText(String.valueOf(t[3]));

            speciesChart.setData(ReportDao.treesBySpecies());
            locationChart.setData(ReportDao.treesByLocation());
            conditionChart.setData(ReportDao.treesByCondition());

            latestModel.setRowCount(0);
            List<Object[]> all = PlantationDao.list("");
            for (int i = 0; i < Math.min(5, all.size()); i++) {
                Object[] r = all.get(i);
                latestModel.addRow(new Object[]{Ui.str(r[1]), r[2], r[3], r[5], r[6]});
            }
        } catch (SQLException e) {
            Ui.error(this, e);
        }
    }
}
