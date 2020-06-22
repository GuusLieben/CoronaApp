package org.dockbox.corona.core.packets.key;

public enum KeyHeaders {
    KEY_PREFIX("KEY::"),
    KEY_OK(KEY_PREFIX.getValue() + "OK"),
    KEY_REJECTED(KEY_PREFIX.getValue() + "REJECT");

    private final String value;

    KeyHeaders(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
