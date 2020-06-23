package org.dockbox.corona.cli.central;

import org.dockbox.corona.core.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class CentralCLI {

    public static final PrivateKey CENTRAL_CLI_PRIVATE = Util.storePubAndGetKey(getPublicKeyFile()).get();
    public static final int LISTENER_PORT = 9191;
    public static final Properties properties = new Properties();
    public static String CONNECTION_STRING;

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        properties.load(new FileReader("cli_config.properties"));
        String connectionUrl =
                "jdbc:sqlserver://"
                        + properties.getProperty("db_host")
                        + "\\SQLEXPRESS:" + properties.getProperty("db_port")
                        + ";user=" + properties.getProperty("db_user")
                        + ";password=" + properties.getProperty("db_password")
                        + ";database=" + properties.getProperty("db_name");

        Connection con = DriverManager.getConnection(connectionUrl);
        System.out.println(con.prepareStatement("SELECT 1").execute());
    }

    public static File getPublicKeyFile() {
        return new File("central_cli.pub");
    }

}
