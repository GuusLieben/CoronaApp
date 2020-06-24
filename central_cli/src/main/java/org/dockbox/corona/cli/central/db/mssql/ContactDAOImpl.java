package org.dockbox.corona.cli.central.db.mssql;

import org.dockbox.corona.cli.central.db.ContactDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContactDAOImpl implements ContactDAO {

    @Override
    public List<String> getAllContactByID(String ID) throws SQLException {
        ResultSet rs = MSSQLQueries.GET_ALL_CONTACTS_BY_ID.prepare(ID, ID);

        List<String> contacts = new ArrayList<>();
        while(rs.next()) {
            contacts.add(rs.getString("Contacts"));
        }
        return contacts;
    }

    @Override
    // Returns true if the query succeeded
    public boolean createContact(String userID1, String userID2) throws SQLException {
        ResultSet rs = MSSQLQueries.CREATE_CONTACT.prepare(userID1, userID2, LocalDateTime.now(), userID1, userID2);

        rs.next();
        return (userID1.equals(rs.getString("ID_user_1")) && userID2.equals(rs.getString("ID_user_2")));
    }
}
