package org.dockbox.corona.cli.central.network;

import org.dockbox.corona.cli.central.CentralCLI;
import org.dockbox.corona.core.network.NetworkListener;
import org.dockbox.corona.core.packets.SendContactConfPacket;
import org.dockbox.corona.core.packets.SendInfectConfPacket;
import org.dockbox.corona.core.packets.SendUserDataPacket;
import org.dockbox.corona.core.util.Util;

import java.io.IOException;
import java.net.DatagramSocket;

public class CLINetworkListener extends NetworkListener {

    private final DatagramSocket socket;

    public CLINetworkListener() throws IOException, InstantiationException {
        super(CentralCLI.CENTRAL_CLI_PRIVATE);
        this.socket = new DatagramSocket(9191);
    }

    @Override
    protected void handlePacket(String rawPacket, SessionHandler session) {
        Runnable invalidPacket = () -> sendDatagram(Util.INVALID, true, session.getRemote(), session.getRemotePort());
        String decryptedPacket = Util.decryptPacket(rawPacket, getPrivateKey(), session.getSessionKey());

        if (Util.INVALID.equals(decryptedPacket) && !Util.isUnmodified(Util.getContent(decryptedPacket), Util.getHash(decryptedPacket)))
            invalidPacket.run();

        String header = Util.getHeader(decryptedPacket);
        String content = Util.getContent(decryptedPacket);

        if (SendContactConfPacket.EMPTY.getHeader().equals(header)) {
            SendContactConfPacket sccp = SendContactConfPacket.EMPTY.deserialize(content);
            // ..
        } else if (SendInfectConfPacket.EMPTY.getHeader().equals(header)) {
            SendInfectConfPacket sicp = SendInfectConfPacket.EMPTY.deserialize(content);
            // ..
        } else if (SendUserDataPacket.EMPTY.getHeader().equals(header)) {
            SendUserDataPacket sudp = SendUserDataPacket.EMPTY.deserialize(content);
            // ..
        } else invalidPacket.run();
    }

    @Override
    protected DatagramSocket getSocket() {
        return this.socket;
    }
}
