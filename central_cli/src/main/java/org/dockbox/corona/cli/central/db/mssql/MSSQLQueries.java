package org.dockbox.corona.cli.central.db.mssql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum MSSQLQueries {
    // User queries
    CHECK_USER_EXISTS_BY_ID("SELECT CASE WHEN EXISTS (SELECT ID FROM [User] WHERE ID = ? ) THEN 'TRUE' ELSE 'FALSE' END AS 'Exists'"),
    CREATE_USER("INSERT INTO [User] VALUES ( ? )");

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
