package org.dockbox.corona.cli.central.db;

import org.dockbox.corona.core.model.UserData;

public interface UserDAO {
     public UserData getUserByID(int ID);
     public UserData createUser(int ID);
}
