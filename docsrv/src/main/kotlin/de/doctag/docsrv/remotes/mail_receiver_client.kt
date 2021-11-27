package de.doctag.docsrv.remotes

import de.doctag.docsrv.extractDocumentIdAndSplitDocument
import de.doctag.docsrv.extractDocumentIds
import de.doctag.docsrv.extractTextFromPdf
import de.doctag.docsrv.model.*
import de.doctag.lib.toSha1HexString
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.lang.Exception
import java.time.ZonedDateTime
import java.util.*
import javax.mail.*
import javax.mail.Session
import javax.mail.search.FlagTerm

val logger = LoggerFactory.getLogger("de.doctag.lib.mail_receiver");

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
        logger.info("Marking message with subject ${message.subject} as read")
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
                processMessage(msg, null)
                recv?.markAsRead(msg)
            }

            logger.info ("Processing mails done. Processed ${messages?.size} mails")
        }
    }

    private fun processMessage(msg: Message, fromAddress: String?){
        logger.info ("Checking if message from ${msg.from?.first()} / ${msg.sentDate?.toString()} with content Type ${msg.contentType}")

        val content = msg.content
        when(content){
            is String->{
                logger.info("Received content of type string")
            }
            is Multipart ->{
                logger.info("Received content of type multipart. Handling each part")
                processMultipart(content, fromAddress ?: msg.from?.first()?.toString())
            }
        }
    }

    private fun processMultipart(multiPart: Multipart, fromAddress: String?){
        for (i in 0 until multiPart.count) {

            val part = multiPart.getBodyPart(i)
            logger.info("Processing multipart $i / ${multiPart.count} of type ${part.disposition}")

            when (val content = part.content){
                is String->{
                    logger.info("Received content of type string")
                }
                is InputStream ->{
                    kweb.logger.info("Received content of type inputStream. ${part.fileName}")
                    if(part.fileName?.toLowerCase()?.endsWith(".pdf") == true) {
                        logger.info("Importing attachment ${part.fileName}")
                        processInputStream(part.fileName, content, fromAddress)
                    }
                }
                is Message -> {
                    logger.info("Received content of type Message")
                    processMessage(content, fromAddress)
                }
                is Multipart -> {
                    logger.info("Received content of type Multipart")
                    processMultipart(content, fromAddress)
                }
            }
        }
    }

    private fun DocumentId?.isOwnedByThisMachine() = this?.hostname == dbContext.currentConfig.hostname
    private fun DocumentId?.isOwnedByOtherMachine() = this != null && this.hostname != dbContext.currentConfig.hostname
    private fun DocumentId?.isNotPresent() = this != null && this.hostname == dbContext.currentConfig.hostname

    private fun processInputStream(fileName: String, stream: InputStream, fromAddress: String?){
        try {
            val rawDocumentData = Base64.getEncoder().encodeToString(stream.readBytes())

            extractDocumentIdAndSplitDocument(rawDocumentData).forEach{

                val documentData = it.b64

                val doctagId = it.documentId
                logger.info("Found doctag -> ${doctagId.fullUrl}")

                if (dbContext.documents.countDocuments(Document::url eq doctagId.fullUrl) != 0L) {
                    logger.info("Document already imported. Skipping  $doctagId")
                    return
                }

                val fd = FileData(
                    _id = documentData.toSha1HexString(),
                    name = fileName,
                    base64Content = documentData,
                    contentType = "application/pdf"
                )
                fd.apply { dbContext.files.save(fd) }


                when {
                    doctagId.isOwnedByThisMachine() -> {
                        kweb.logger.info("Importing document which is owned by this host.")
                        val doc = Document()
                        doc._id = doctagId.id
                        doc.url = doctagId.fullUrl

                        doc.attachmentId = fd._id
                        doc.attachmentHash = fd.base64Content?.toSha1HexString()
                        doc.originalFileName = fileName
                        doc.created = ZonedDateTime.now()
                        doc.fullText = fd.base64Content?.let { extractTextFromPdf(it) }
                        doc.workflow = dbContext.currentConfig.workflow?.defaultWorkflowId?.let {
                            dbContext.workflows.findOneById(
                                it
                            )
                        }
                        doc.tags = doc.fullText.determineMatchingTags(dbContext.tags.find().toList())

                        doc.apply { dbContext.documents.save(doc) }
                    }
                    doctagId.isOwnedByOtherMachine() -> {
                        logger.info("Importing document which is not owned by this host.")

                        val docSignRequest = DocumentSignRequest(
                            doctagUrl = doctagId.fullUrl,
                            createdBy = DocumentSignRequestUser(
                                userId = null,
                                userName = fromAddress
                            ),
                            timestamp = ZonedDateTime.now(),
                            role = null
                        )
                        dbContext.signRequests.save(docSignRequest)
                    }
                    doctagId.isNotPresent() -> {
                        logger.info("Importing document where no doctag is present")
                    }
                    else -> {
                        logger.info("Unknown case")
                    }
                }
            }
        }
        catch(ex:Exception){
            logger.error("Failed to Process message. Error: ${ex.message}", ex)
        }
    }
}