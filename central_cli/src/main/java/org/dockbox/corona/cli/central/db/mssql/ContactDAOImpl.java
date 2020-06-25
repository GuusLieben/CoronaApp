package org.dockbox.corona.cli.central.db.mssql;

import org.dockbox.corona.cli.central.db.ContactDAO;
import org.dockbox.corona.cli.central.util.MSSQLUtil;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContactDAOImpl implements ContactDAO {

    private final MSSQLUtil instance;

    public ContactDAOImpl(MSSQLUtil instance) {
        this.instance = instance;
    }

    @Override
    public List<String> getAllContactByID(String ID) throws SQLException {
        ResultSet rs = MSSQLQueries.GET_ALL_CONTACTS_BY_ID.prepare(instance, ID, ID);

        List<String> contacts = new ArrayList<>();
        while(rs.next()) {
            contacts.add(rs.getString("Contacts"));
        }
        return contacts;
    }

    @Override
    // Returns true if the query succeeded
    public boolean createContact(String userID1, String userID2, Date dateOfContact) throws SQLException {
        ResultSet rs = MSSQLQueries.CREATE_CONTACT.prepare(instance, userID1, userID2, dateOfContact, userID1, userID2);

        rs.next();
        return (userID1.equals(rs.getString("ID_user_1")) && userID2.equals(rs.getString("ID_user_2")));
    }
}
