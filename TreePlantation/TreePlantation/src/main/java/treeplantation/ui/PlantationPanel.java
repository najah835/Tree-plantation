package treeplantation.ui;

import treeplantation.dao.Item;
import treeplantation.dao.LocationDao;
import treeplantation.dao.PlantationDao;
import treeplantation.dao.PlanterDao;
import treeplantation.dao.SpeciesDao;

import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/** Plantation Management: what was planted, where, when, by whom, and how many. */
final class PlantationPanel extends CrudPanel {
    private final JComboBox<Item> species = new JComboBox<>();
    private final JComboBox<Item> location = new JComboBox<>();
    private final JComboBox<Item> planter = new JComboBox<>();
    private final JSpinner date = new JSpinner(new SpinnerDateModel());
    private final JSpinner count = new JSpinner(new SpinnerNumberModel(1, 1, 10_000_000, 1));

    PlantationPanel() {
        super("Plantations", "Record what was planted, where, when, by whom and how many.",
                "plantation",
                new String[]{"Date", "Species", "Location", "Planted by", "Trees", "Condition"});
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd"));
        addField("Tree species *", species);
        addField("Location *", location);
        addField("Planted by *", planter);
        addField("Plantation date *", date);
        addField("Number of trees *", count);
        table.getColumnModel().getColumn(5).setCellRenderer(new ConditionRenderer());
        clearInputs();
    }

    @Override
    protected void reloadLookups() throws SQLException {
        Ui.fill(species, SpeciesDao.items());
        Ui.fill(location, LocationDao.items());
        Ui.fill(planter, PlanterDao.items());
    }

    @Override
    protected List<Object[]> fetch(String text) throws SQLException {
        return PlantationDao.list(text);
    }

    /** Raw columns: 0 id, 1 date, 2 species, 3 location, 4 planter, 5 count, 6 condition, 7-9 ids. */
    @Override
    protected Object[] displayRow(Object[] r) {
        return new Object[]{Ui.str(r[1]), r[2], r[3], r[4], r[5], r[6]};
    }

    @Override
    protected void populate(Object[] r) {
        date.setValue(Ui.toUtilDate(Ui.toLocalDate(r[1])));
        count.setValue(Ui.toInt(r[5]));
        Ui.select(species, Ui.toInt(r[7]));
        Ui.select(location, Ui.toInt(r[8]));
        Ui.select(planter, Ui.toInt(r[9]));
    }

    @Override
    protected void clearInputs() {
        date.setValue(new Date());
        count.setValue(1);
        if (species.getItemCount() > 0) species.setSelectedIndex(0);
        if (location.getItemCount() > 0) location.setSelectedIndex(0);
        if (planter.getItemCount() > 0) planter.setSelectedIndex(0);
    }

    @Override
    protected void persist(Integer id) throws SQLException, ValidationException {
        Item s = (Item) species.getSelectedItem();
        Item l = (Item) location.getSelectedItem();
        Item p = (Item) planter.getSelectedItem();
        if (s == null) throw new ValidationException("Add a tree species first (Tree species screen).");
        if (l == null) throw new ValidationException("Add a location first (Locations screen).");
        if (p == null) throw new ValidationException("Add a person or organization first.");

        LocalDate day = Ui.toLocalDate(date.getValue());
        if (day.isAfter(LocalDate.now())) {
            throw new ValidationException("The plantation date cannot be in the future.");
        }
        int trees = ((Number) count.getValue()).intValue();
        if (id == null) PlantationDao.insert(s.id(), l.id(), p.id(), day, trees);
        else PlantationDao.update(id, s.id(), l.id(), p.id(), day, trees);
    }

    @Override
    protected void deleteRecord(int id) throws SQLException {
        PlantationDao.delete(id);
    }
}
