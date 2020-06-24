package org.dockbox.corona.cli.central.db;

import org.dockbox.corona.core.model.Contact;

import java.util.List;

public interface ContactDAO {
    public List<String> getAllContactByID();
    public Contact createContact(int userID1, int userID2);
}
