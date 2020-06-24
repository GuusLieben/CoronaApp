package org.dockbox.corona.cli.central.util;

import org.dockbox.corona.core.model.UserData;
import org.dockbox.corona.core.packets.RequestUserDataPacket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

public class MSSQLUtil extends CLIUtil {

    public static final Logger log = LoggerFactory.getLogger(MSSQLUtil.class);

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
    public static Connection openConnection(String connectionUrl) throws SQLException {
        Connection con = DriverManager.getConnection(connectionUrl);
        if (con.prepareStatement("SELECT 1").execute()) {
            String dbName = getValueFromConnectionString(connectionUrl, "database");
            String user = getValueFromConnectionString(connectionUrl, "user");
            log.info("Connected to database " + dbName + " as '" + user + "'");
        }
        return con;
    }

    public static void closeConnection(Connection con) {
        try {
            con.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static String getValueFromConnectionString(String connectionString, String property) {
        String[] split = connectionString.split(property+'=');
        return split[1].split(";")[0];
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