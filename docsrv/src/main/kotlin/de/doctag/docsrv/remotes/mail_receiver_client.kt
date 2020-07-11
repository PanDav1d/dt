package de.doctag.docsrv.remotes

import de.doctag.docsrv.model.InboundMailProtocol
import de.doctag.lib.logger
import java.lang.Exception
import java.util.*
import javax.mail.*
import javax.mail.search.FlagTerm


class MailReceiver(val store: Store){

    companion object {
        fun connect(protocol: InboundMailProtocol, server: String, user: String, password : String) : MailReceiver? {
            try {
                val (port, protocol) = when (protocol) {
                    InboundMailProtocol.IMAP -> 993 to "imaps"
                    InboundMailProtocol.POP3 -> 995 to "pop3s"
                }

                logger.info("Connecting $protocol to ${server} port $port user $user password $password")

                val session: Session = Session.getDefaultInstance(Properties())
                val store: Store = session.getStore(protocol)
                store.connect(server, port, user, password)

                return MailReceiver(store)
            }
            catch (ex:Exception){
                logger.error("Failed to connect to Mail server. Reason ${ex.message}")
            }
            return null
        }
    }

    fun receive() : List<Message> {
        val inbox: Folder = store.getFolder("INBOX")
        inbox.open(Folder.READ_ONLY)

        val messages: Array<Message> = inbox.search(
                FlagTerm(Flags(Flags.Flag.SEEN), false))

        messages.sortBy { it.receivedDate }

        return messages.toList()
    }

    fun markAsRead(message: Message){

        val inbox: Folder = store.getFolder("INBOX")

        val flags = Flags()
        flags.add(Flags.Flag.SEEN)
        inbox.setFlags(listOf(message.messageNumber).toIntArray(), flags, true)
    }
}