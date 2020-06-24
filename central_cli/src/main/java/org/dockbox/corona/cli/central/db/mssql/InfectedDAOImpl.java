package org.dockbox.corona.cli.central.db.mssql;

import org.dockbox.corona.cli.central.db.InfectedDAO;
import org.dockbox.corona.core.model.InfectedUser;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InfectedDAOImpl implements InfectedDAO {
    @Override
    public InfectedUser getInfectedByID(String ID) throws SQLException {
        ResultSet rs = MSSQLQueries.GET_INFECTED_BY_ID.prepare(ID);

        InfectedUser.Builder builder = new InfectedUser.Builder();
        rs.next();
        builder.withDateOfInfection(rs.getDate("Date_of_Infection"))
                .withId(rs.getString("ID"))
                .withBSN(rs.getString("BSN"))
                .withFirstName(rs.getString("First_Name"))
                .withLastName(rs.getString("Last_Name"))
                .withBirthDate(rs.getDate("Date_of_Birth"));

        InfectedUser iu = builder.build();
        return iu;
    }

    @Override
    public boolean createInfected(InfectedUser infected) throws SQLException {
        ResultSet rs = MSSQLQueries.CREATE_INFECTED.prepare(infected.getId(),
                infected.getFirstName(),
                infected.getLastName(),
                infected.getBirthDate(),
                infected.getBSN(),
                infected.getDateOfInfection());

        rs.next();
        return infected.getId().equals(rs.getString("ID"));
    }
}
