package org.dockbox.corona.cli.central.db;

import org.dockbox.corona.core.model.InfectedUser;

import java.sql.SQLException;

public interface InfectedDAO {
    public InfectedUser getInfectedByID(String ID) throws SQLException;
    public boolean createInfected(InfectedUser infected) throws SQLException;
}
