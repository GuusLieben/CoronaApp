package org.dockbox.corona.core.packets

abstract class Packet {
    abstract val header: String?
    abstract fun serialize(): String?
    abstract fun deserialize(message: String?): Packet?

    companion object {
        const val HASH_PREFIX = "HASH::"
    }
}
