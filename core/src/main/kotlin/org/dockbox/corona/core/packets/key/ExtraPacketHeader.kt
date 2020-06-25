package org.dockbox.corona.core.packets.key

enum class ExtraPacketHeader(val value: String) {
    KEY_PREFIX("KEY::"),
    KEY_OK(KEY_PREFIX.value + "OK"),
    KEY_REJECTED(KEY_PREFIX.value + "REJECT"),
    DENIED_PREFIX("DENIED::"),
    DENIED_UNREQUESTED(DENIED_PREFIX.value + "UNREQUESTED_DATA"),
    FAILED_PREFIX("FAILED::"),
    FAILED_UNAVAILABLE(FAILED_PREFIX.value + "USER_UNAVAILABLE"),
    CONTACT_PREFIX("CONTACT::"),
    LOGIN_FAILED(DENIED_PREFIX.value + "LOGIN_INVALID")
}
