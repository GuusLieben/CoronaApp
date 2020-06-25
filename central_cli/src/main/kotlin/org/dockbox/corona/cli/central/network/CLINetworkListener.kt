package org.dockbox.corona.cli.central.network

import org.dockbox.corona.cli.central.CentralCLI
import org.dockbox.corona.cli.central.util.CLIUtil
import org.dockbox.corona.cli.central.util.MSSQLUtil
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
        private val locations: MutableMap<String, Session> = ConcurrentHashMap()
        private val queuedInfections: MutableMap<String, Date> = ConcurrentHashMap()
        private val logins: MutableMap<String, Pair<String, String>> = ConcurrentHashMap();
    }

    private var util: CLIUtil = MSSQLUtil()
    private val socket: DatagramSocket = DatagramSocket(CentralCLI.LISTENER_PORT)

    override fun handlePacket(rawPacket: String, session: Session) {
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
            Util.decryptPacket(rawPacket, session.remotePublicKey, session.sessionKey)

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
        val now = Date.from(Instant.now())

        when {
            SendContactConfPacket.EMPTY.header == header -> { // Receive from client
                val sccp = SendContactConfPacket.EMPTY.deserialize(content)!!

                val confirmDiff: Long = abs(sccp.contactReceived.time - sccp.contactSent.time)
                val diff = TimeUnit.SECONDS.convert(confirmDiff, TimeUnit.MILLISECONDS)
                if (confirmDiff > 60000) log.warn("Contact confirmation took " + (diff / 1000) + "s!")

                if (util.addAndVerify(sccp.id, sccp.contactId)) util.addContactToDatabase(
                    sccp.id,
                    sccp.contactId,
                    sccp.contactReceived
                )
                val confirmPacket = ConfirmPacket(sccp, Date.from(Instant.now()))
                sendPacket(
                    confirmPacket,
                    false,
                    false,
                    session.remote,
                    session.remotePort,
                    false,
                    session.remotePublicKey,
                    session.sessionKey
                )

                // Server needs to be able to alert a user
                locations[sccp.id] = session
            }

            SendInfectConfPacket.EMPTY.header == header -> { // Receive from client
                val sicp = SendInfectConfPacket.EMPTY.deserialize(content)!!

                val receiveDiff: Long = abs(now.time - sicp.infected.time)
                val diff = TimeUnit.SECONDS.convert(receiveDiff, TimeUnit.MILLISECONDS)
                if (diff > 30000) log.warn("Packet took " + (diff / 1000) + "s to arrive!")

                queuedInfections[sicp.id] = sicp.infected
                val confirmPacket = ConfirmPacket(sicp, now)
                sendPacket(confirmPacket, false, false, session.remote, session.remotePort, false, session.remotePublicKey, session.sessionKey)
            }

            SendUserDataPacket.EMPTY.header == header -> { // Receive from client
                val util: CLIUtil =
                    MSSQLUtil(MSSQLUtil.properties.getProperty("db_user"), MSSQLUtil.properties.getProperty("db_pass"))
                val sudp = SendUserDataPacket.EMPTY.deserialize(content)!!

                if (userDataQueue.contains(sudp.userData.id) && queuedInfections.containsKey(sudp.userData.id)) {

                    util.addInfectedToDatabase(sudp.userData, queuedInfections[sudp.userData.id]!!)
                    val confirmPacket = ConfirmPacket(sudp, now)
                    sendPacket(
                        confirmPacket,
                        false,
                        false,
                        session.remote,
                        session.remotePort,
                        false,
                        session.remotePublicKey,
                        session.sessionKey
                    )

                    val requestedBySessions = userDataQueue[sudp.userData.id]
                    requestedBySessions!!.forEach {
                        sendPacket(
                            sudp,
                            false,
                            false,
                            it.remote,
                            it.remotePort + 1,
                            false,
                            session.remotePublicKey,
                            session.sessionKey
                        )
                    }
                    userDataQueue.remove(sudp.userData.id)

                } else {
                    log.warn("Received unrequested data from " + session.remote.hostAddress)
                    sendDatagram(
                        ExtraPacketHeader.DENIED_UNREQUESTED.value,
                        true,
                        session.remote,
                        session.remotePort,
                        false,
                        session.remotePublicKey
                    )
                }
            }

            RequestUserDataPacket.EMPTY.header == header -> { // Forward from GGD
                if (loggedIn) {
                    val login = logins[remoteAddress]!!
                    val util: CLIUtil = MSSQLUtil(login.first, login.second)
                    val rudp = RequestUserDataPacket.EMPTY.deserialize(content)!!
                    // Clients will only accept this request if they have indicated they are infected. When they indicate
                    // a infection their IP and port are stored. Otherwise this information is not available.
                    if (locations.containsKey(rudp.id) && util.verifySession(login.first, login.second)) {
                        val loc = locations[rudp.id]!!
                        sendPacket(
                            rudp,
                            false,
                            false,
                            loc.remote,
                            loc.remotePort,
                            false,
                            loc.remotePublicKey,
                            loc.sessionKey
                        )
                    } else {
                        log.warn("Requested data for user " + rudp.id + " but user is not currently active or source is not authorised")
                        sendDatagram(
                            ExtraPacketHeader.FAILED_UNAVAILABLE.value,
                            true,
                            session.remote,
                            session.remotePort,
                            false,
                            session.remotePublicKey
                        )
                    }
                } else {
                    sendDatagram(
                        ExtraPacketHeader.NOT_LOGGED_IN.value,
                        false,
                        session.remote,
                        session.remotePort,
                        false,
                        session.remotePublicKey
                    )
                }
            }

            SendAlertPacket.EMPTY.header == header -> {
                if (loggedIn) {
                    val login = logins[remoteAddress]!!
                    val util: CLIUtil = MSSQLUtil(login.first, login.second)
                    val sap = SendAlertPacket.EMPTY.deserialize(content)!!
                    if (locations.containsKey(sap.id) && util.verifySession(login.first, login.second)) {
                        val loc = locations[sap.id]!!
                        sendPacket(
                            sap,
                            false,
                            false,
                            loc.remote,
                            loc.remotePort,
                            false,
                            loc.remotePublicKey,
                            loc.sessionKey
                        )
                    } else {
                        log.warn("Requested alert for user " + sap.id + " but user is not currently active or source is not authorised")
                        sendDatagram(
                            ExtraPacketHeader.FAILED_UNAVAILABLE.value,
                            true,
                            session.remote,
                            session.remotePort,
                            false,
                            session.remotePublicKey
                        )
                    }
                } else {
                    sendDatagram(
                        ExtraPacketHeader.NOT_LOGGED_IN.value,
                        false,
                        session.remote,
                        session.remotePort,
                        false,
                        session.remotePublicKey
                    )
                }
            }

            LoginPacket.EMPTY.header == header -> {
                val lp = LoginPacket.EMPTY.deserialize(content)!!
                val util = MSSQLUtil(lp.userName, lp.password)
                try {
                    util.openConnection().close()
                    logins[remoteAddress] = Pair(lp.userName, lp.password)
                    sendPacket(
                        ConfirmPacket<LoginPacket>(lp, Date.from(Instant.now())),
                        false,
                        false,
                        session.remote,
                        session.remotePort,
                        false,
                        session.remotePublicKey,
                        session.sessionKey
                    )
                } catch (exception: SQLException) {
                    sendDatagram(
                        ExtraPacketHeader.LOGIN_FAILED.value,
                        false,
                        session.remote,
                        session.remotePort,
                        false,
                        session.remotePublicKey
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
