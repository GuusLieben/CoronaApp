package org.dockbox.corona.cli.central.network;

import org.dockbox.corona.cli.central.CentralCLI;
import org.dockbox.corona.core.network.NetworkListener;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.DatagramSocket;

public class CLINetworkListener extends NetworkListener {

    private final DatagramSocket socket;

    public CLINetworkListener() throws IOException, InstantiationException {
        super(CentralCLI.CENTRAL_CLI_PRIVATE);
        this.socket = new DatagramSocket(9191);
    }

    @Override
    protected void handlePacket(String rawPacket) {

    }

    @Override
    protected SecretKey getSessionKey() {
        return null;
    }

    @Override
    protected DatagramSocket getSocket() {
        return this.socket;
    }
}
