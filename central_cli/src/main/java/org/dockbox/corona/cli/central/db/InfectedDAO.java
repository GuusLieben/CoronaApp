package org.dockbox.corona.cli.central.db;

import org.dockbox.corona.core.model.InfectedUser;

public interface InfectedDAO {
    public InfectedUser getInfectedByID(String ID);
    public InfectedUser createInfected(InfectedUser infected);
}
