package org.dockbox.corona.cli.central.util;

import org.dockbox.corona.core.model.UserData;
import org.dockbox.corona.core.packets.RequestUserDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class MSSQLUtil extends CLIUtil {





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
