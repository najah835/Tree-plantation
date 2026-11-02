package treeplantation.ui;

import treeplantation.dao.SpeciesDao;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.sql.SQLException;
import java.util.List;

/** Tree Species Management. */
final class SpeciesPanel extends CrudPanel {
    private final JTextField common = new JTextField();
    private final JTextField scientific = new JTextField();
    private final JComboBox<String> type =
            new JComboBox<>(new String[]{"Fruit", "Timber", "Medicinal", "Ornamental", "Shade"});

    SpeciesPanel() {
        super("Tree species", "Add, update, view and delete the species that can be planted.",
                "species", new String[]{"Common name", "Scientific name", "Type"});
        addField("Common name *", common);
        addField("Scientific name", scientific);
        addField("Type", type);
    }

    @Override
    protected List<Object[]> fetch(String text) throws SQLException {
        return SpeciesDao.search(text);
    }

    @Override
    protected void populate(Object[] r) {
        common.setText(Ui.str(r[1]));
        scientific.setText(Ui.str(r[2]));
        type.setSelectedItem(Ui.str(r[3]));
    }

    @Override
    protected void clearInputs() {
        common.setText("");
        scientific.setText("");
        type.setSelectedIndex(0);
    }

    @Override
    protected void persist(Integer id) throws SQLException, ValidationException {
        String name = required(common, "Enter the common name of the species.");
        String sci = scientific.getText().trim();
        String kind = (String) type.getSelectedItem();
        if (id == null) SpeciesDao.insert(name, sci, kind);
        else SpeciesDao.update(id, name, sci, kind);
    }

    @Override
    protected void deleteRecord(int id) throws SQLException {
        SpeciesDao.delete(id);
    }
}
