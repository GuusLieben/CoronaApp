package org.dockbox.corona.core.packets;

import org.jetbrains.annotations.Nullable;

public class LoginPacket extends Packet {

    public static final LoginPacket EMPTY = new LoginPacket(null, null);

    private final String userName;
    private final String password;

    public LoginPacket(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getHeader() {
        return "SEND::LOGIN";
    }

    @Override
    public String serialize() {
        return new StringBuilder()
                .append("USERNAME=").append(userName)
                .append("\nPASSWORD=").append(password)
                .toString();
    }

    @Override
    public LoginPacket deserialize(String message) {
        String[] lines = message.split("\n");
        LoginPacket.Builder builder = new LoginPacket.Builder();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            String key = keyValue[0];
            String value = keyValue[1];

            switch (key) {
                case "USERNAME":
                    builder.withUserName(value);
                    break;
                case "PASSWORD":
                    builder.withPassword(value);
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect packet format");
            }
        }
        return builder.build();
    }


    public static final class Builder {
        private String userName;
        private String password;

        private Builder() {
        }

        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public LoginPacket build() {
            return new LoginPacket(userName, password);
        }
    }
}
