package org.dockbox.corona.cli.central.db.mssql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum MSSQLQueries {
    // User queries
    CHECK_USER_EXISTS_BY_ID("SELECT CASE WHEN EXISTS (SELECT ID FROM [User] WHERE ID = ? ) THEN 'TRUE' ELSE 'FALSE' END AS 'Exists'"),
    CREATE_USER("INSERT INTO [User] VALUES ( ? ); SELECT ID FROM [User] WHERE ID = ? "),

    // Contact queries
    GET_ALL_CONTACTS_BY_ID("SELECT ID_user_1 AS 'Contacts' FROM [Contact] WHERE ID_user_2 = ? UNION SELECT ID_user_2 FROM [Contact] WHERE ID_user_1 = ? "),
    CREATE_CONTACT("INSERT INTO [Contact] VALUES( ? , ? , ? )"),

    // Infected queries
    GET_INFECTED_BY_ID("SELECT ID, BSN, First_Name, Last_Name, Date_of_Birth, Date_of_Infection FROM [Infected] WHERE ID = ? "),
    CREATE_INFECTED("INSERT INTO [Infected] VALUES( ? , ? , ? , ? , ? , ? )");

    private final String query;

    MSSQLQueries(String query) {
        this.query = query;
    }

    public PreparedStatement prepare(Connection conn, Object... args) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(this.query);
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            statement.setObject(i + 1, obj);
        }
        return statement;
    }
}
