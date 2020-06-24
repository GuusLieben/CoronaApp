package org.dockbox.corona.cli.central.util;

import org.dockbox.corona.core.model.UserData;
import org.dockbox.corona.core.packets.RequestUserDataPacket;
import org.dockbox.corona.core.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import static org.dockbox.corona.cli.central.CentralCLI.getPublicKeyFile;

public class MSSQLUtil extends CLIUtil {

    public static final Properties properties = new Properties();
    static {
        try {
            properties.load(new FileReader("cli_config.properties"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    public static final String MSSQL_CONNECTION_STRING = "jdbc:sqlserver://"
            + properties.getProperty("db_host")
            + "\\SQLEXPRESS:" + properties.getProperty("db_port")
            + ";user=" + properties.getProperty("db_user")
            + ";password=" + properties.getProperty("db_password")
            + ";database=" + properties.getProperty("db_name");

    @NotNull
    public static Connection getConnection(String connectionUrl) throws SQLException {
        Connection con = DriverManager.getConnection(connectionUrl);
        if (con.prepareStatement("SELECT 1").execute()) System.out.println("Connected to Database CoronaApp as user == " + properties.getProperty("db_user"));
        return con;
    }

    @Override
    public void addContactToDatabase(@NotNull String senderId, @NotNull String contactId, @NotNull Date timeOfContact) {

    }

    @Override
    public void addInfectedToDatabase(@NotNull String senderId, @NotNull Date timeInfected) {

    }

    @Override
    public void addUserToDatabase(@NotNull UserData userData) {

    }

    @Override
    public boolean verifyRequest(@NotNull RequestUserDataPacket requestPacket) {
        return false;
    }
}
