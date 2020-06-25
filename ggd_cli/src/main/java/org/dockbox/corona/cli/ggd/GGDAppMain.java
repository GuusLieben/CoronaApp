package org.dockbox.corona.cli.ggd;

import org.dockbox.corona.cli.ggd.network.GGDAppNetworkListener;
import org.dockbox.corona.core.network.TCPConnection;
import org.dockbox.corona.core.packets.*;
import org.dockbox.corona.core.packets.key.ExtraPacketHeader;
import org.dockbox.corona.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Date;
import java.time.Instant;
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

    private static final Logger log = LoggerFactory.getLogger(GGDAppMain.class);
    private static final Scanner scanner = new Scanner(System.in);

    private static String userName;
    private static String password;

    public static void main(String[] args) throws IOException, InstantiationException {
        server = InetAddress.getByName("127.0.0.1");

        System.out.print("Enter port to listen on : ");
        APP_PORT = Integer.parseInt(scanner.nextLine());

        while (true) {
            System.out.print("Please enter your Username and Password : \n- Username : ");
            userName = (scanner.nextLine());

            System.out.println("- Password : ");
            password = (scanner.nextLine());

            System.out.println(" > Attempting to log in ....");

            // Verify the login credentials
            TCPConnection conn = new TCPConnection(privateKey, publicKey, server.getHostAddress(), serverPort, false, APP_PORT);
            conn.initiateKeyExchange();
            LoginPacket lp = new LoginPacket(userName, password);
            String res = conn.sendPacket(lp, false, false, true);
            conn.getSocket().close();

            if (ExtraPacketHeader.LOGIN_FAILED.getValue().equals(res)) {
                System.out.println("Login failed");
            } else {
                ConfirmPacket<LoginPacket> clp = new ConfirmPacket<LoginPacket>().deserialize(res, LoginPacket.EMPTY);
                System.out.println("Confirmed login at " + clp.getConfirmed().toString());
                break;
            }
        }

        new Thread(GGDAppMain.main).start();
//        new GGDAppNetworkListener(privateKey).listen();
    }


    @Override
    public void run() {
        System.out.println("Enter a command > ");
        String command = scanner.nextLine();
        switch (command.split(" ")[0]) {
            case "help":
                System.out.println("======================");
                System.out.println("contacts <id> -> Request all the contacts of the specified id");
                System.out.println("alert <id> -> Alert an user to request userdata");
                System.out.println("quit -> exit the program");
                System.out.println("======================");
                break;
            case "contacts":
                System.out.println("- Enter User ID : ");
                String user = scanner.nextLine();
                System.out.println(" > Attempting to retrieve contacts .... ");
                try {
                    RequestContactsPacket rcp = new RequestContactsPacket(user);
                    TCPConnection conn = new TCPConnection(privateKey, publicKey, server.getHostAddress(), serverPort, false, APP_PORT);
                    conn.initiateKeyExchange();
                    conn.getSocket().setSoTimeout(30000);
                    String res = conn.sendPacket(rcp, false, false, true);
                    conn.getSocket().setSoTimeout(0);
                    if (Util.INVALID.equals(res)) log.error("Received invalid response from server!");
                    else {
                        SendContactsPacket scp = SendContactsPacket.EMPTY.deserialize(res);
                        log.info("Contacts of User " + user + " in the last 3 weeks : ");
                        scp.contacts.forEach(log::info);
                    }
                    conn.getSocket().close();
                } catch (IOException | InstantiationException e) {
                    e.printStackTrace();
                    log.error(" Command failed : " + e.getMessage());
                }
                break;
            case "alert":
                System.out.println("- Enter User ID : ");
                String id = scanner.nextLine();
                System.out.println(" > Attempting to alert user .... ");
                try {
                    SendAlertPacket sap = new SendAlertPacket(id, Date.from(Instant.now()));
                    TCPConnection conn = new TCPConnection(privateKey, publicKey, server.getHostAddress(), serverPort, false, APP_PORT);
                    conn.initiateKeyExchange();
                    String res = conn.sendPacket(sap, false, false, false);
                    if (Util.INVALID.equals(res)) log.error("Received invalid response from server!");
                    else {
                        ConfirmPacket<? extends Packet> confirmPacket = new ConfirmPacket<>().deserialize(res, SendAlertPacket.EMPTY);
                        java.util.Date confirmed = confirmPacket.getConfirmed();
                        log.info("Confirmed at " + confirmed.toString());
                    }
                    conn.getSocket().close();
                } catch (IOException | InstantiationException e) {
                    e.printStackTrace();
                    log.error(" Command failed : " + e.getMessage());
                }
                log.info("\n > Command handled successfully\n");
                break;
            case "quit":
                System.out.println(" > Goodbye :)");
                return;
        }
    }
}
