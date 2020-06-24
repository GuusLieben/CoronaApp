package org.dockbox.corona.cli.central.network

import org.dockbox.corona.cli.central.CentralCLI
import org.dockbox.corona.cli.central.util.CLIUtil
import org.dockbox.corona.cli.central.util.SimpleCLIUtil
import org.dockbox.corona.core.network.NetworkListener
import org.dockbox.corona.core.packets.*
import org.dockbox.corona.core.packets.key.ExtraPacketHeader
import org.dockbox.corona.core.util.Util
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class CLINetworkListener : NetworkListener(CentralCLI.CENTRAL_CLI_PRIVATE) {

    companion object {
        // IDs, requested by Sessions
        private val userDataQueue: MutableMap<String, MutableList<Session>> = ConcurrentHashMap()
        private val locations: MutableMap<String, Pair<InetAddress, Int>> = ConcurrentHashMap()
    }

    private var util: CLIUtil = SimpleCLIUtil()
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
            Util.decryptPacket(rawPacket, session.remotePublicKey, session.sessionKey)

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
                sendPacket(confirmPacket, false, false, session.remote, session.remotePort, false)
            }

            SendInfectConfPacket.EMPTY.header == header -> { // Receive from client
                val sicp = SendInfectConfPacket.EMPTY.deserialize(content)!!

                val receiveDiff: Long = abs(now.time - sicp.infected.time)
                val diff = TimeUnit.SECONDS.convert(receiveDiff, TimeUnit.MILLISECONDS)
                if (diff > 30000) log.warn("Packet took " + (diff / 1000) + "s to arrive!")

                util.addInfectedToDatabase(sicp.id, sicp.infected)
                val confirmPacket = ConfirmPacket(sicp, now)
                sendPacket(confirmPacket, false, false, session.remote, session.remotePort, false)

                // Do not keep track of their location until a infection is indicated
                locations[sicp.id] = Pair(session.remote, session.remotePort)
            }

            SendUserDataPacket.EMPTY.header == header -> { // Receive from client
                val sudp = SendUserDataPacket.EMPTY.deserialize(content)!!

                if (userDataQueue.contains(sudp.user.id)) {
                    util.addUserToDatabase(sudp.user)
                    val confirmPacket = ConfirmPacket(sudp, now)
                    sendPacket(confirmPacket, false, false, session.remote, session.remotePort, false)

                    val requestedBySessions = userDataQueue[sudp.user.id]
                    requestedBySessions!!.forEach { sendPacket(sudp, false, false, it.remote, it.remotePort, false) }
                    userDataQueue.remove(sudp.user.id)
                } else {
                    log.warn("Received unrequested data from " + session.remote.hostAddress)
                    sendDatagram(
                        ExtraPacketHeader.DENIED_UNREQUESTED.value,
                        true,
                        session.remote,
                        session.remotePort,
                        false
                    )
                }
            }

            RequestUserDataPacket.EMPTY.header == header -> { // Forward from GGD
                val rudp = RequestUserDataPacket.EMPTY.deserialize(content)!!
                // Clients will only accept this request if they have indicated they are infected. When they indicate
                // a infection their IP and port are stored. Otherwise this information is not available.
                if (locations.containsKey(rudp.id) && util.verifyRequest(rudp)) {
                    val loc = locations[rudp.id]!!
                    sendPacket(rudp, false, false, loc.first, loc.second, false)
                } else {
                    log.warn("Requested data for user " + rudp.id + " but user is not currently active or did not indicate to be infected")
                    sendDatagram(
                        ExtraPacketHeader.FAILED_UNAVAILABLE.value,
                        true,
                        session.remote,
                        session.remotePort,
                        false
                    )
                }
            }
            else -> invalidPacket.run()
        }
    }

    override fun getSocket(): DatagramSocket {
        return this.socket
    }

}
