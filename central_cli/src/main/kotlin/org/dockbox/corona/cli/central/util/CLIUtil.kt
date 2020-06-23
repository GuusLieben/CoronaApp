package org.dockbox.corona.cli.central.util

import org.dockbox.corona.core.model.User
import org.dockbox.corona.core.packets.RequestUserDataPacket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

abstract class CLIUtil {

    companion object {
        private val contactQueue: MutableMap<String, MutableList<String>> = ConcurrentHashMap();
    }

    fun addAndVerify(senderId: String, contactId: String): Boolean {
        // If contact is in queue
        //  - check if they have our sender listed
        //    if they do, remove the entry, and return true
        //    if they don't, proceed
        // If not
        //  - Check if our sender is in queue
        //    if they are, add our contact to their list
        //    if they aren't, create a new entry and add contact
        // return false

        if (contactQueue.containsKey(contactId)) {
            val ids = contactQueue[contactId];
            if (ids!!.contains(senderId)) {
                ids.remove(senderId)
                return true
            }
        }

        if (!contactQueue.containsKey(senderId)) contactQueue[senderId] = ArrayList()
        contactQueue[senderId]!!.add(contactId)

        return false;
    }

    abstract fun addContactToDatabase(senderId: String, contactId: String, timeOfContact: Date)

    abstract fun addInfectedToDatabase(senderId: String, timeInfected: Date)

    abstract fun addUserToDatabase(user: User)

    abstract fun verifyRequest(requestPacket: RequestUserDataPacket): Boolean
}
