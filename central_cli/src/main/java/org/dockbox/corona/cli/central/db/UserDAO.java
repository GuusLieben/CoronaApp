package org.dockbox.corona.cli.central.db;

import java.sql.SQLException;

public interface UserDAO {
     public boolean checkUserExistsByID(String ID) throws SQLException;
     public String createUser(String ID) throws SQLException;
}
