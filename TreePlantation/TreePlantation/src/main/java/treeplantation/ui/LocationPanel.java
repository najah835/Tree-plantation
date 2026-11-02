package treeplantation.ui;

import treeplantation.dao.LocationDao;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.sql.SQLException;
import java.util.List;

/** Location Management. */
final class LocationPanel extends CrudPanel {
    private final JTextField name = new JTextField();
    private final JTextField area = new JTextField();
    private final JComboBox<String> type = new JComboBox<>(
            new String[]{"Campus", "School", "Public park", "Roadside", "Community land", "Other"});

    LocationPanel() {
        super("Locations", "Manage the places where trees are planted.",
                "location", new String[]{"Location", "Area", "Place type"});
        addField("Location name *", name);
        addField("Area or city *", area);
        addField("Place type", type);
    }

    @Override
    protected List<Object[]> fetch(String text) throws SQLException {
        return LocationDao.search(text);
    }

    @Override
    protected void populate(Object[] r) {
        name.setText(Ui.str(r[1]));
        area.setText(Ui.str(r[2]));
        type.setSelectedItem(Ui.str(r[3]));
    }

    @Override
    protected void clearInputs() {
        name.setText("");
        area.setText("");
        type.setSelectedIndex(0);
    }

    @Override
    protected void persist(Integer id) throws SQLException, ValidationException {
        String n = required(name, "Enter the location name.");
        String a = required(area, "Enter the area or city.");
        String kind = (String) type.getSelectedItem();
        if (id == null) LocationDao.insert(n, a, kind);
        else LocationDao.update(id, n, a, kind);
    }

    @Override
    protected void deleteRecord(int id) throws SQLException {
        LocationDao.delete(id);
    }
}
