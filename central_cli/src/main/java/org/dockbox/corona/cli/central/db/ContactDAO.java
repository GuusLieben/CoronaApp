package org.dockbox.corona.cli.central.db;

import org.dockbox.corona.core.model.Contact;

import java.util.List;

public interface ContactDAO {
    public List<String> getAllContactByID(String ID);
    public Contact createContact(String userID1, String userID2);
}
