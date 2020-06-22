package org.dockbox.corona.cli.central.network

import org.dockbox.corona.cli.central.CentralCLI
import org.dockbox.corona.core.network.NetworkListener
import org.dockbox.corona.core.packets.RequestUserDataPacket
import org.dockbox.corona.core.packets.SendContactConfPacket
import org.dockbox.corona.core.packets.SendInfectConfPacket
import org.dockbox.corona.core.packets.SendUserDataPacket
import org.dockbox.corona.core.util.Util
import java.net.DatagramSocket

class CLINetworkListener : NetworkListener(CentralCLI.CENTRAL_CLI_PRIVATE) {

    private val socket: DatagramSocket = DatagramSocket(CentralCLI.LISTENER_PORT)

    override fun handlePacket(rawPacket: String, session: Session) {
        val invalidPacket = Runnable {
            sendDatagram(
                Util.INVALID,
                true,
                session.remote,
                session.remotePort
            )
        }

        val decryptedPacket =
            Util.decryptPacket(rawPacket, privateKey, session.sessionKey)

        if (Util.INVALID == decryptedPacket && !Util.isUnmodified(
                Util.getContent(decryptedPacket),
                Util.getHash(decryptedPacket)
            )
        ) invalidPacket.run()

        val header = Util.getHeader(decryptedPacket)
        val content = Util.getContent(decryptedPacket)
        when {
            SendContactConfPacket.EMPTY.header == header -> { // Receive from client
                val sccp = SendContactConfPacket.EMPTY.deserialize(content)
                // ..
            }
            SendInfectConfPacket.EMPTY.header == header -> { // Receive from client
                val sicp = SendInfectConfPacket.EMPTY.deserialize(content)
                // ..
            }
            SendUserDataPacket.EMPTY.header == header -> { // Receive from client
                val sudp = SendUserDataPacket.EMPTY.deserialize(content)
                // ..
                // Check if data was previously requested by GGD
            }
            RequestUserDataPacket.EMPTY.header == header -> { // Forward from GGD
                val rudp = RequestUserDataPacket.EMPTY.deserialize(content)
                // ..
                // Verify identity and store in Queue
            }
            else -> invalidPacket.run()
        }
    }

    override fun getSocket(): DatagramSocket {
        return this.socket
    }

}
