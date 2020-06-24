package org.dockbox.corona.cli.central;

import org.dockbox.corona.cli.central.db.mssql.MSSQLQueries;
import org.dockbox.corona.cli.central.util.MSSQLUtil;
import org.dockbox.corona.core.util.Util;

import org.dockbox.corona.cli.central.db.mssql.MSSQLQueries;
import org.dockbox.corona.core.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.sql.*;
import java.util.Properties;

public class CentralCLI {

    public static final PrivateKey CENTRAL_CLI_PRIVATE = Util.storePubAndGetKey(getPublicKeyFile()).get();
    public static final int LISTENER_PORT = 9191;

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        Connection con = MSSQLUtil.getConnection(MSSQLUtil.MSSQL_CONNECTION_STRING);

        // Example query usage
        String id = "2222222222";
        ResultSet rs = MSSQLQueries.CHECK_USER_EXISTS_BY_ID.prepare(con, id).executeQuery();
        while (rs.next()) {
            System.out.println(rs.getBoolean("Exists"));
        }
    }

    public static File getPublicKeyFile() {
        return new File("central_cli.pub");
    }

}
