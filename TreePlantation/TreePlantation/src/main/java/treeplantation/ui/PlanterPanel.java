package treeplantation.ui;

import treeplantation.dao.PlanterDao;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.sql.SQLException;
import java.util.List;

/** Person / Organization Management. */
final class PlanterPanel extends CrudPanel {
    private final JTextField name = new JTextField();
    private final JComboBox<String> kind = new JComboBox<>(new String[]{"Person", "Organization"});
    private final JTextField contact = new JTextField();

    PlanterPanel() {
        super("People and organizations", "Record who takes part in plantation activities.",
                "person or organization", new String[]{"Name", "Kind", "Contact"});
        addField("Name *", name);
        addField("Kind", kind);
        addField("Contact (phone or email)", contact);
    }

    @Override
    protected List<Object[]> fetch(String text) throws SQLException {
        return PlanterDao.search(text);
    }

    @Override
    protected void populate(Object[] r) {
        name.setText(Ui.str(r[1]));
        kind.setSelectedItem(Ui.str(r[2]));
        contact.setText(Ui.str(r[3]));
    }

    @Override
    protected void clearInputs() {
        name.setText("");
        kind.setSelectedIndex(0);
        contact.setText("");
    }

    @Override
    protected void persist(Integer id) throws SQLException, ValidationException {
        String n = required(name, "Enter the name.");
        String k = (String) kind.getSelectedItem();
        String c = contact.getText().trim();
        if (id == null) PlanterDao.insert(n, k, c);
        else PlanterDao.update(id, n, k, c);
    }

    @Override
    protected void deleteRecord(int id) throws SQLException {
        PlanterDao.delete(id);
    }
}
