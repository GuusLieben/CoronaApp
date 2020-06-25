package org.dockbox.corona.app;

import org.dockbox.corona.app.network.UserAppNetworkListener;
import org.dockbox.corona.core.model.UserData;
import org.dockbox.corona.core.network.TCPConnection;
import org.dockbox.corona.core.packets.ConfirmPacket;
import org.dockbox.corona.core.packets.Packet;
import org.dockbox.corona.core.packets.SendInfectConfPacket;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Scanner;

public class UserAppMain implements Runnable {

    private static final KeyPair keyPair = Util.generateKeyPair().get();
    private static final PrivateKey privateKey = keyPair.getPrivate();
    private static final PublicKey publicKey = keyPair.getPublic();

    public static InetAddress server;
    public static int serverPort = 9191;

    public static final PublicKey serverPublic = Util.getPublicKeyFromFile(new File("central_cli.pub")).get();

    private UserData user;

    public static final UserAppMain main = new UserAppMain();
    public static int APP_PORT;

    private static final Logger log = LoggerFactory.getLogger(UserAppMain.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InstantiationException, ParseException {
        server = InetAddress.getByName("127.0.0.1");

        System.out.print("Enter port to listen on : ");
        APP_PORT = Integer.parseInt(scanner.nextLine());

        UserData.Builder builder = new UserData.Builder();
        System.out.print("User data :\n- Firstname : ");
        builder.withFirstName(scanner.nextLine());

        System.out.print("- Lastname : ");
        builder.withLastName(scanner.nextLine());

        System.out.print("- BSN : ");
        builder.withBSN(scanner.nextLine());

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        System.out.print("- Birthdate (dd-MM-yyyy) : ");
        builder.withBirthDate(format.parse(scanner.nextLine()));

        System.out.print("- ID : ");
        builder.withId(scanner.nextLine());

        main.user = builder.build();

        new Thread(UserAppMain.main).start();
        new UserAppNetworkListener(privateKey).listen();
    }

    public UserData getUser() {
        return user;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public void run() {
        while (true) {
            System.out.print(" > ");
            String command = scanner.nextLine();
            switch (command.split(" ")[0]) {
                case "help":
                    System.out.println("======================");
                    System.out.println("infect -> Notify the server you have been infected");
                    System.out.println("secret -> Display a secret");
                    System.out.println("contact <id> -> Send a contact request to another user");
                    System.out.println("======================");
                    break;
                case "secret":
                    System.out.println("                               ,,\n" +
                            "               ,▄▄██▀▀       ▄████▄\n" +
                            "            ▄█████▀      ,▄▀▀████████▄∞\n" +
                            "         ╓██████`   ▄▄█████U   █▌▀▀`\n" +
                            "       ,██████▀      ██████U   █▌        ▄,          ,▄     ▄          ▄▄          ,▄      ▄,           ▄▄,\n" +
                            "      ╔██████▌       ██████U   █▌  ▄▄▄████████▄▄▄ ~██████▄████▄▄ ▄▄▄█████████▄▄ -██████▄▄██████▄  ╓▄▄█████████▄▄\n" +
                            "     ╒███████        ██████U   █▌  █████  `▀████▌   █████⌐ ▀▀▀▀` ▐████  `▀█████   █████▀  ▀█████   ████▌ `▀█████\n" +
                            "     ████████        ██████U   █▌  █████   ▐████▌   █████        ▐████    ████▌   █████U   █████   ████▌   █████\n" +
                            "     ████████        ██████U   █▌  █████   ▐████▌   █████        ▐████    ████▌   █████U   █████   ████▌   █████\n" +
                            "     ████████▌       ██████U   █▌  █████   ▐████▌   █████        ▐████    ████▌   █████U   █████   ████▌   █████\n" +
                            "     ▐████████▄      ██████    █▌  █████   ▐████▌   █████        ▐████    ████▌   █████U   █████   ████▌   █████\n" +
                            "      ▀█████████▄    ████▀     █▌ ▄███████▄▄████▌   █████,      ╓███████▄▄█████   █████▌   █████  ╓███████▄█████▄,,\n" +
                            "       ▀██████████▄▄██▀        █▌     ▀▀█████▀      ▀███▀           \"▀█████▀\"     ▀███▀    ▀███▀     ▀▀████▀\"████▀\n" +
                            "         ▀████████████▄▄▄,,,,,,██@\"       ▀           ▀                 ▀           ╙        ▀          '▀     ▀\n" +
                            "            ▀█████████████████▀█▌\n" +
                            "                 ▀▀▀▀▀▀▀▀▀   ▄▄█▌\n" +
                            "                            ████▌");
                    break;
                case "infect":
                    try {
                        SendInfectConfPacket sicp = new SendInfectConfPacket(getUser().getId(), Date.from(Instant.now()));
                        TCPConnection conn = new TCPConnection(privateKey, publicKey, server.getHostAddress(), serverPort, false, APP_PORT);
                        String res = conn.sendPacket(sicp, false, false, true);
                        if (Util.INVALID.equals(res)) log.error("Received invalid response from server!");
                        else {
                            ConfirmPacket<? extends Packet> confirmPacket = new ConfirmPacket<>().deserialize(res, SendInfectConfPacket.EMPTY);
                            java.util.Date confirmed = confirmPacket.getConfirmed();
                            log.info("Confirmed at " + confirmed.toString());
                        }
                        conn.getSocket().close();
                    } catch (IOException | InstantiationException e) {
                        e.printStackTrace();
                    }
                    break;
                case "contact":
                    try {
                        System.out.print(" - Port : ");
                        int port = Integer.parseInt(scanner.nextLine());
                        TCPConnection conn = new TCPConnection(null, null, "127.0.0.1", port + 1, false, APP_PORT);
                        String datagram = ExtraPacketHeader.CONTACT_PREFIX.getValue() + getUser().getId();
                        conn.sendDatagram(datagram, true, false);
                        conn.getSocket().close();
                    } catch (IOException | InstantiationException e) {
                        e.printStackTrace();
                    }
                    break;
                case "quit":
                    return;
            }
        }
    }
}
