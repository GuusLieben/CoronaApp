package org.dockbox.corona.cli.central.db;

import org.dockbox.corona.core.model.UserData;

public interface UserDAO {
     public UserData checkUserExistsByID(int ID);
     public UserData createUser(int ID);
}
