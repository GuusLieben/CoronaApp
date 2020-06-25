package org.dockbox.corona.app.network

import org.dockbox.corona.app.UserAppMain
import org.dockbox.corona.core.network.NetworkListener
import org.dockbox.corona.core.network.TCPConnection
import org.dockbox.corona.core.packets.*
import org.dockbox.corona.core.packets.key.ExtraPacketHeader
import org.dockbox.corona.core.util.Util
import java.net.DatagramSocket
import java.net.SocketException
import java.security.PrivateKey
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class UserAppNetworkListener(privateKey: PrivateKey?) : NetworkListener(privateKey) {

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

        if (rawPacket.startsWith(ExtraPacketHeader.CONTACT_PREFIX.value)) {
            val contactId = rawPacket.replaceFirst(ExtraPacketHeader.CONTACT_PREFIX.value, "")
            val sccp = SendContactConfPacket(UserAppMain.main.user.id, contactId, Date.from(Instant.now()), Date.from(Instant.now()))
            val conn = TCPConnection(UserAppMain.getPrivateKey(), UserAppMain.getPublicKey(), UserAppMain.server.hostAddress, UserAppMain.serverPort, false, UserAppMain.APP_PORT)
            conn.initiateKeyExchange()
            val res = conn.sendPacket(sccp, false, false, true)
            log.warn(res)
            conn.socket.close()
            return
        }

        val decryptedPacket =
            Util.decryptPacket(rawPacket, UserAppMain.serverPublic, session.sessionKey)

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
            SendAlertPacket.EMPTY.header == header -> { // Receive from server
                val sap = SendAlertPacket.EMPTY.deserialize(content)!!
                if (sap.id == UserAppMain.main.user.id) log.error("You have been in contact with a person with Corona, and have therefore been instructed to stay at home!")
            }
            RequestUserDataPacket.EMPTY.header == header -> {
                val rudp = RequestUserDataPacket.EMPTY.deserialize(content)!!
                if (rudp.id == UserAppMain.main.user.id) {
                    val receiveDiff: Long = abs(now.time - rudp.timestamp.time)
                    var diff = TimeUnit.SECONDS.convert(receiveDiff, TimeUnit.MILLISECONDS)
                    if (diff > 30000) log.warn("Packet took " + (diff / 1000) + "s to arrive!")

                    val sudp = SendUserDataPacket(UserAppMain.main.user, Date.from(Instant.now()))
                    val response = sendPacket(sudp, false, false, session.remote, session.remotePort, true, UserAppMain.serverPublic, session.sessionKey)
                    val sudpC = ConfirmPacket<SendUserDataPacket>().deserialize(response, SendUserDataPacket.EMPTY)

                    val confirmDiff: Long = abs(now.time - sudpC.confirmed.time)
                    diff = TimeUnit.SECONDS.convert(confirmDiff, TimeUnit.MILLISECONDS)
                    if (diff > 30000) log.warn("Confirmation took " + (diff / 1000) + "s to arrive!")

                    log.info("Received confirmation for userdata")
                }
            } else -> invalidPacket.run()
        }
    }

    override fun getSocket(): DatagramSocket {
        return socket!!
    }

    init {
        try {
            socket = DatagramSocket(UserAppMain.APP_PORT + 1)
        } catch (e: SocketException) {
            e.printStackTrace()
        }
    }
}
