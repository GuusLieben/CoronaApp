package org.dockbox.corona.app;

import org.dockbox.corona.app.network.UserAppNetworkListener;
import org.dockbox.corona.core.model.User;
import org.dockbox.corona.core.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class UserAppMain implements Runnable {

    public final String id = String.valueOf(this.hashCode());
    private static final KeyPair keyPair = Util.generateKeyPair().get();
    private static final PrivateKey privateKey = keyPair.getPrivate();
    private static final PublicKey publicKey = keyPair.getPublic();

    public static InetAddress server;
    public static int serverPort = 9191;

    public static final PublicKey serverPublic = Util.getPublicKeyFromFile(new File("central_cli.pub")).get();

    private User user;

    public static final UserAppMain main = new UserAppMain();
    public static int APP_PORT;

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InstantiationException, ParseException {
        server = InetAddress.getByName("127.0.0.1");

        System.out.print("Enter port to listen on : ");
        APP_PORT = Integer.parseInt(scanner.nextLine());

        User.Builder builder = new User.Builder();
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

    public User getUser() {
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
                // TODO : Send infection packet
                break;
            case "contact":
                // TODO : Send contact request
                break;
        }
    }
}
