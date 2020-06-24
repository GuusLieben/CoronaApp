package org.dockbox.corona.cli.central;

import org.dockbox.corona.cli.central.db.mssql.MSSQLQueries;
import org.dockbox.corona.cli.central.util.MSSQLUtil;
import org.dockbox.corona.core.model.InfectedUser;
import org.dockbox.corona.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CentralCLI {

    public static final Logger log = LoggerFactory.getLogger(CentralCLI.class);
    public static final PrivateKey CENTRAL_CLI_PRIVATE = Util.storePubAndGetKey(getPublicKeyFile()).get();
    public static final int LISTENER_PORT = 9191;

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        Connection con = MSSQLUtil.getConnection(MSSQLUtil.MSSQL_CONNECTION_STRING);

        // Example query usage
//        String id = "2222222222";
//        ResultSet rs = MSSQLQueries.CHECK_USER_EXISTS_BY_ID.prepare(con, id).executeQuery();
//        while (rs.next()) {
//            System.out.println(rs.getBoolean("Exists"));
//        }

//        MSSQLQueries.CREATE_CONTACT.prepare(con, "1234567890", "1111111111", LocalDateTime.now()).execute();

//        ResultSet contacts = MSSQLQueries.GET_ALL_CONTACTS_BY_ID.prepare(con, "1111111111", "1111111111").executeQuery();
//        while(contacts.next()) {
//            System.out.println(contacts.getString("Contacts"));
//        }

//        ResultSet infected = MSSQLQueries.GET_INFECTED_BY_ID.prepare(con, "1111111111").executeQuery();
//        InfectedUser.Builder builder = new InfectedUser.Builder();
//        while(infected.next()) {
//            builder.withDateOfInfection(infected.getDate("Date_of_Infection"))
//                    .withId(infected.getString("ID"))
//                    .withBSN(infected.getString("BSN"))
//                    .withFirstName(infected.getString("First_Name"))
//                    .withLastName(infected.getString("Last_Name"))
//                    .withBirthDate(infected.getDate("Date_of_Birth"));
//        }
//        InfectedUser iu = builder.build();
//        System.out.println(iu.toString());

//        MSSQLQueries.CREATE_INFECTED.prepare(con,  "2222222222", "Guus", "Lieben", "1969-04-20", "888888888", "2021-04-26").execute();
    }

    public static File getPublicKeyFile() {
        return new File("central_cli.pub");
    }

}
