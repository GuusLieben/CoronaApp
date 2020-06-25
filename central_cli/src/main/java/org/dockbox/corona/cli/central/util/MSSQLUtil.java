package org.dockbox.corona.cli.central.util;

import org.dockbox.corona.cli.central.db.ContactDAO;
import org.dockbox.corona.cli.central.db.InfectedDAO;
import org.dockbox.corona.cli.central.db.UserDAO;
import org.dockbox.corona.cli.central.db.mssql.ContactDAOImpl;
import org.dockbox.corona.cli.central.db.mssql.InfectedDAOImpl;
import org.dockbox.corona.cli.central.db.mssql.UserDAOImpl;
import org.dockbox.corona.core.model.InfectedUser;
import org.dockbox.corona.core.model.UserData;
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

    public final ContactDAO contactDAO= new ContactDAOImpl(this);
    public final InfectedDAO infectedDAO = new InfectedDAOImpl(this);
    public final UserDAO userDAO = new UserDAOImpl(this);

    private final String mssqlConnectionString;

    public MSSQLUtil(String user, String password) {
        this.mssqlConnectionString = "jdbc:sqlserver://"
                + properties.getProperty("db_host")
                + "\\SQLEXPRESS:" + properties.getProperty("db_port")
                + ";user=" + user
                + ";password=" + password
                + ";database=" + properties.getProperty("db_name");
    }

    public static final Properties properties = new Properties();
    static {
        try {
            properties.load(new FileReader("cli_config.properties"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public Connection openConnection() throws SQLException {
        return openConnection(this.mssqlConnectionString);
    }

    @NotNull
    public Connection openConnection(String connectionUrl) throws SQLException {
        Connection con = DriverManager.getConnection(connectionUrl);
        if (con.prepareStatement("SELECT 1").execute()) {
            String dbName = getValueFromConnectionString(connectionUrl, "database");
            String user = getValueFromConnectionString(connectionUrl, "user");
            log.info("Connected to database " + dbName + " as '" + user + "'");
        }
        return con;
    }

    private static String getValueFromConnectionString(String connectionString, String property) {
        String[] split = connectionString.split(property+'=');
        return split[1].split(";")[0];
    }

    @Override
    public void addContactToDatabase(@NotNull String senderId, @NotNull String contactId, @NotNull Date timeOfContact) {
        addUserToDatabase(senderId);
        addUserToDatabase(contactId);

        try {
            log.info("Attempting to add a new Contact to the Database: " + senderId + " | " + contactId + " | " + timeOfContact);
            contactDAO.createContact(senderId, contactId, (java.sql.Date) timeOfContact);
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
            throwables.printStackTrace();
        }
        log.info("Contact added successfully");
    }

    @Override
    public void getAllContactsFromDatabaseById(@NotNull String userId) {
        try {
            log.info("Attempting to get all Contacts from the database for user: " + userId);
            contactDAO.getAllContactByID(userId);
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
            throwables.printStackTrace();
        }
    }

    @Override
    public void addInfectedToDatabase(@NotNull UserData userData, @NotNull Date timeInfected) {
        try {
            log.info("Attempting to add a new InfectedUser to the Database: " + userData.toString() + " | " + timeInfected);
            infectedDAO.createInfected(new InfectedUser(userData, timeInfected));
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
            throwables.printStackTrace();
        }
        log.info("InfectedUser added successfully");
    }

    @Override
    public void addUserToDatabase(@NotNull String senderId) {
        try {
            log.info("Attemping to add a new User to the Database: " + senderId);
            userDAO.createUser(senderId);
        } catch (SQLException throwables) {
            log.error(throwables.getMessage());
            throwables.printStackTrace();
        }
        log.info("User added successfully");
    }

    @Override
    public boolean verifySession(@NotNull String userName, @NotNull String password) {
        if (
                userName.equals(getValueFromConnectionString(this.mssqlConnectionString, "user"))
                && password.equals(getValueFromConnectionString(this.mssqlConnectionString, "password"))
        ) {
            try {
                openConnection().close();
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }
}
