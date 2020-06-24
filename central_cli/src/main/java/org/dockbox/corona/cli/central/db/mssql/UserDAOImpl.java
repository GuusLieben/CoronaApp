package org.dockbox.corona.cli.central.db.mssql;

import org.dockbox.corona.cli.central.db.UserDAO;
import org.dockbox.corona.cli.central.util.MSSQLUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {


    @Override
    public boolean checkUserExistsByID(String ID) throws SQLException {
        Connection con = MSSQLUtil.openConnection(MSSQLUtil.MSSQL_CONNECTION_STRING);
        ResultSet rs = MSSQLQueries.CHECK_USER_EXISTS_BY_ID.prepare(con, ID).executeQuery();
        MSSQLUtil.closeConnection(con);

        rs.next();
        return rs.getBoolean("Exists");
    }

    @Override
    public String createUser(String ID) throws SQLException {
        Connection con = MSSQLUtil.openConnection(MSSQLUtil.MSSQL_CONNECTION_STRING);
        ResultSet rs = MSSQLQueries.CREATE_USER.prepare(con, ID, ID).executeQuery();
        MSSQLUtil.closeConnection(con);

        rs.next();
        return rs.getString("ID");
    }
}
