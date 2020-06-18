package org.dockbox.corona.cli.central;

import org.dockbox.corona.core.util.PacketSecurityUtil;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

public class CentralCLI {

    public static final PrivateKey CENTRAL_CLI_PRIVATE = PacketSecurityUtil.storePubAndGetKey(getPublicKeyFile());

    public static void main(String[] args) {
        // Sample usage
        PublicKey publicKey = PacketSecurityUtil.getPublicKeyFromFile(getPublicKeyFile());
    }

    public static File getPublicKeyFile() {
        return new File("central_cli.pub");
    }

}
