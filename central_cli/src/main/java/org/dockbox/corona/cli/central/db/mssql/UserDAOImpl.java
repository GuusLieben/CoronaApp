package org.dockbox.corona.cli.central.db.mssql;

import org.dockbox.corona.cli.central.db.UserDAO;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean checkUserExistsByID(String ID) throws SQLException {
        ResultSet rs = MSSQLQueries.CHECK_USER_EXISTS_BY_ID.prepare(ID);

        rs.next();
        return rs.getBoolean("Exists");
    }

    @Override
    // Returns true if the query succeeded
    public boolean createUser(String ID) throws SQLException {
        ResultSet rs = MSSQLQueries.CREATE_USER.prepare(ID, ID);

        rs.next();
        return (ID.equals(rs.getString("ID")));
    }
}
