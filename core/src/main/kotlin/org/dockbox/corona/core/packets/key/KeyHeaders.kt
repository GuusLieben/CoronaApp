package org.dockbox.corona.core.packets.key

enum class KeyHeaders(val value: String) {
    KEY_PREFIX("KEY::"), KEY_OK(KEY_PREFIX.value + "OK"), KEY_REJECTED(KEY_PREFIX.value + "REJECT");
}
