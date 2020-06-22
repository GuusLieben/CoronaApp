package org.dockbox.corona.cli.central;

import org.dockbox.corona.core.util.Util;

import java.io.File;
import java.security.PrivateKey;

public class CentralCLI {

    public static final PrivateKey CENTRAL_CLI_PRIVATE = Util.storePubAndGetKey(getPublicKeyFile()).get();
    public static final int LISTENER_PORT = 9191;

    public static void main(String[] args) {

    }

    public static File getPublicKeyFile() {
        return new File("central_cli.pub");
    }

}
