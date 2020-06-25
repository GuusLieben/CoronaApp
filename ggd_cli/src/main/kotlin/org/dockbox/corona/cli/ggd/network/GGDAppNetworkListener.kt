package org.dockbox.corona.cli.ggd.network

import org.dockbox.corona.cli.ggd.GGDAppMain
import org.dockbox.corona.core.network.NetworkListener
import org.dockbox.corona.core.packets.SendContactsPacket
import org.dockbox.corona.core.packets.SendUserDataPacket
import org.dockbox.corona.core.util.Util
import java.net.DatagramSocket
import java.net.SocketException
import java.security.PrivateKey
import java.util.function.Consumer

class GGDAppNetworkListener(privateKey: PrivateKey?) : NetworkListener(privateKey) {

    private var socket: DatagramSocket? = null

    override fun handlePacket(
        rawPacket: String,
        session: Session
    ) {
        val invalidPacket = Runnable {
            sendDatagram(
                Util.INVALID,
                true,
                session.remote,
                session.remotePort,
                null
            )
        }

        val decryptedPacket =
            Util.decryptPacket(rawPacket, GGDAppMain.serverPublic, session.sessionKey)

        if (Util.INVALID == decryptedPacket || !Util.isUnmodified(
                Util.getContent(decryptedPacket),
                Util.getHash(decryptedPacket)
            )
        ) {
            log.warn("Received modified or corrupted packet")
            invalidPacket.run()
        }

        val header = Util.getHeader(decryptedPacket)
        val content = Util.getContent(decryptedPacket)

        when {
            SendContactsPacket.EMPTY.header == header -> {
                val scp = SendContactsPacket.EMPTY.deserialize(content)!!
                log.info("Contacts of User ${scp.userId} in the last 3 weeks : ")
                scp.contacts.forEach(Consumer(log::info))
            }
            SendUserDataPacket.EMPTY.header == header -> {
                val sudp = SendUserDataPacket.EMPTY.deserialize(content)!!
                log.info("Received user data for ${sudp.userData.id} :")
                log.info(sudp.userData.toString())
            }
            else -> invalidPacket.run()
        }
    }

    override fun getSocket(): DatagramSocket {
        return socket!!
    }

    init {
        try {
            socket = DatagramSocket(GGDAppMain.APP_PORT + 1)
        } catch (e: SocketException) {
            e.printStackTrace()
        }
    }
}
