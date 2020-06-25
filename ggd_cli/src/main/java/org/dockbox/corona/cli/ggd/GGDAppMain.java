package org.dockbox.corona.cli.ggd;

import org.dockbox.corona.core.util.Util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class GGDAppMain implements Runnable {

    public final String id = String.valueOf(this.hashCode());
    private static final KeyPair keyPair = Util.generateKeyPair().get();
    private static final PrivateKey privateKey = keyPair.getPrivate();
    private static final PublicKey publicKey = keyPair.getPublic();

    public static InetAddress server;
    public static int serverPort = 9191;

    public static final GGDAppMain main = new GGDAppMain();
    public static final PublicKey serverPublic = Util.getPublicKeyFromFile(new File("central_cli.pub")).get();
    public static int APP_PORT;

    private static final Scanner scanner = new Scanner(System.in);

    private static String userName;
    private static String password;

    public static void main(String[] args) throws UnknownHostException {
        server = InetAddress.getByName("127.0.0.1");

        System.out.print("Enter port to listen on : ");
        APP_PORT = Integer.parseInt(scanner.nextLine());

        System.out.print("Please enter your Username and Password : \n- Username : ");
        userName = (scanner.nextLine());

        System.out.println("- Password : ");
        password = (scanner.nextLine());

        new Thread(GGDAppMain.main).start();
//        new GGDAppNetworkListener(privateKey).listen();
    }


    @Override
    public void run() {
        System.out.println("Enter a command > ");
        String command = scanner.nextLine();
        switch (command.split(" ")[0]) {

        }
    }
}
