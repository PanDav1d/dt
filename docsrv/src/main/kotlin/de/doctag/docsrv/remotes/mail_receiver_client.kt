package de.doctag.docsrv.remotes

import de.doctag.docsrv.extractDocumentIdOrNull
import de.doctag.docsrv.model.*
import de.doctag.lib.logger
import org.litote.kmongo.eq
import org.litote.kmongo.save
import java.io.InputStream
import java.lang.Exception
import java.time.ZonedDateTime
import java.util.*
import javax.mail.*
import javax.mail.Session
import javax.mail.search.FlagTerm


class MailReceiver(val store: Store){

    companion object {
        fun connect(conf: InboundMailConfig) : MailReceiver? {
            return connect(conf.protocol!!, conf.server!!, conf.user!!, conf.password!!)
        }

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
        inbox.open(Folder.READ_WRITE)

        val flags = Flags()
        flags.add(Flags.Flag.SEEN)
        inbox.setFlags(listOf(message.messageNumber).toIntArray(), flags, true)
    }
}

class AttachmentImporter(val dbContext: DbContext){
    fun runImport(){
        dbContext.currentConfig.inboundMail?.let {
            val recv = MailReceiver.connect(it)
            val messages = recv?.receive()

            messages?.filter { it.contentType.contains("multipart") }?.forEach { msg->
                processMessage(msg)
                recv?.markAsRead(msg)
            }

            kweb.logger.info ("Processing mails done. Processed ${messages?.size} mails")
        }
    }

    private fun processMessage(msg: Message){
        kweb.logger.info ("Checking if message from ${msg.from?.first()} / ${msg.sentDate?.toString()} with content Type ${msg.contentType}")

        val content = msg.content
        when(content){
            is String->{
                kweb.logger.info("Received content of type string")
            }
            is Multipart ->{
                kweb.logger.info("Received content of type multipart. Handling each part")
                processMultipart(content)
            }
        }
    }

    private fun processMultipart(multiPart: Multipart){
        for (i in 0 until multiPart.count) {

            val part = multiPart.getBodyPart(i)
            kweb.logger.info("Processing multipart $i / ${multiPart.count} of type ${part.disposition}")

            when (val content = part.content){
                is String->{
                    kweb.logger.info("Received content of type string")
                }
                is InputStream ->{
                    kweb.logger.info("Received content of type inputStream. ${part.fileName}")
                    if(part.fileName.toLowerCase().endsWith(".pdf")) {
                        kweb.logger.info("Importing attachment ${part.fileName}")
                        processInputStream(part.fileName, content)
                    }
                }
                is Message -> {
                    kweb.logger.info("Received content of type Message")
                    processMessage(content)
                }
                is Multipart -> {
                    kweb.logger.info("Received content of type Multipart")
                    processMultipart(content)
                }
            }
        }
    }

    private fun DocumentId?.isOwnedByThisMaching() = this?.hostname == dbContext.currentConfig.hostname
    private fun DocumentId?.isOwnedByOtherMachine() = this != null && this.hostname == dbContext.currentConfig.hostname
    private fun DocumentId?.isNotPresent() = this != null && this.hostname == dbContext.currentConfig.hostname

    private fun processInputStream(fileName: String, stream: InputStream){
        val documentData = Base64.getEncoder().encodeToString(stream.readBytes())

        val doctagId = extractDocumentIdOrNull(documentData)
        kweb.logger.info("Found doctag -> ${doctagId?.fullUrl ?: "none"}")

        if(doctagId != null && dbContext.documents.countDocuments(Document::url eq doctagId.fullUrl) != 0L){
            kweb.logger.info("Document already imported. Skipping  $doctagId")
            return
        }

        val fd = FileData(_id = null, name = fileName, base64Content = documentData, contentType = "application/pdf")
        fd.apply { dbContext.files.save(fd) }

        val doc = Document()

        when{
            doctagId.isOwnedByThisMaching()-> {
                kweb.logger.info("Importing document which is owned by this host.")
                doc._id = doctagId?.id
                doc.url = doctagId?.fullUrl
            }
            doctagId.isOwnedByOtherMachine() -> {
                kweb.logger.info("Importing document which is not owned by this host.")
                doc.url = doctagId?.fullUrl
                // TODO: Register at remote machine for changes
            }
            doctagId.isNotPresent() -> {
                kweb.logger.info("Importing document where no doctag is present")
            }
        }

        doc.attachmentId = fd._id
        doc.originalFileName = fileName
        doc.created = ZonedDateTime.now()

        doc.apply { dbContext.documents.save(doc) }

        if(doc.url == null) {
            doc.url = "https://${dbContext.currentConfig.hostname}/d/${doc._id}"
        }

        dbContext.documents.save(doc)

        kweb.logger.info("Doc is reachable at ${doc.url}")
    }
}