package org.dockbox.corona.cli.central;

import org.dockbox.corona.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.PrivateKey;

public class CentralCLI {

    public static final Logger log = LoggerFactory.getLogger(CentralCLI.class);
    public static final PrivateKey CENTRAL_CLI_PRIVATE = Util.storePubAndGetKey(getPublicKeyFile()).get();
    public static final int LISTENER_PORT = 9191;

    public static void main(String[] args) {}

    public static File getPublicKeyFile() {
        return new File("central_cli.pub");
    }

}
