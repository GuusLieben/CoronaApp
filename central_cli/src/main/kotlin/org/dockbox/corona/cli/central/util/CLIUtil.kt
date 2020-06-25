package org.dockbox.corona.cli.central.util

import org.dockbox.corona.core.model.UserData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

abstract class CLIUtil {

    private val log: Logger = LoggerFactory.getLogger(CLIUtil::class.java)

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
                log.info("Verified contact between $senderId and $contactId")
                return true
            }
        }

        if (!contactQueue.containsKey(senderId)) contactQueue[senderId] = ArrayList()
        contactQueue[senderId]!!.add(contactId)

        log.info("Queued verification for contact between $senderId and $contactId")

        return false;
    }

    abstract fun addContactToDatabase(senderId: String, contactId: String, timeOfContact: Date)

    abstract fun getAllContactsFromDatabaseById(userId: String)

    abstract fun addInfectedToDatabase(userData: UserData, timeInfected: Date)

    abstract fun addUserToDatabase(senderId: String)

    abstract fun verifySession(userName: String, password: String): Boolean
}
