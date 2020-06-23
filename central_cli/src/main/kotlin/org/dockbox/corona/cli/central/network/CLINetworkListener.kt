package org.dockbox.corona.cli.central.network

import org.dockbox.corona.cli.central.CentralCLI
import org.dockbox.corona.cli.central.util.CLIUtil
import org.dockbox.corona.core.network.NetworkListener
import org.dockbox.corona.core.packets.*
import org.dockbox.corona.core.util.Util
import java.net.DatagramSocket
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class CLINetworkListener : NetworkListener(CentralCLI.CENTRAL_CLI_PRIVATE) {

    private lateinit var util: CLIUtil;
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
        val now = Date.from(Instant.now())
        when {
            SendContactConfPacket.EMPTY.header == header -> { // Receive from client
                val sccp = SendContactConfPacket.EMPTY.deserialize(content)!!

                val confirmDiff: Long = abs(sccp.contactReceived.time - sccp.contactSent.time)
                val diff = TimeUnit.SECONDS.convert(confirmDiff, TimeUnit.MILLISECONDS)
                if (confirmDiff > 60000) log.warn("Contact confirmation took " + (diff/1000) + "s!")

                if (util.addAndVerify(sccp.id, sccp.contactId)) util.addContactToDatabase(sccp.id, sccp.contactId, sccp.contactReceived)
                val confirmPacket = ConfirmPacket(sccp, Date.from(Instant.now()))
                sendPacket(confirmPacket, false, session.remote, session.remotePort, false)
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
