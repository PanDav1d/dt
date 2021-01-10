package de.doctag.docsrv.remotes

import de.doctag.docsrv.extractDocumentIdOrNull
import de.doctag.docsrv.extractTextFromPdf
import de.doctag.docsrv.model.*
import de.doctag.lib.logger
import de.doctag.lib.sha1
import de.doctag.lib.toSha1HexString
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
                processMessage(msg, null)
                recv?.markAsRead(msg)
            }

            kweb.logger.info ("Processing mails done. Processed ${messages?.size} mails")
        }
    }

    private fun processMessage(msg: Message, fromAddress: String?){
        kweb.logger.info ("Checking if message from ${msg.from?.first()} / ${msg.sentDate?.toString()} with content Type ${msg.contentType}")

        val content = msg.content
        when(content){
            is String->{
                kweb.logger.info("Received content of type string")
            }
            is Multipart ->{
                kweb.logger.info("Received content of type multipart. Handling each part")
                processMultipart(content, fromAddress ?: msg.from?.first()?.toString())
            }
        }
    }

    private fun processMultipart(multiPart: Multipart, fromAddress: String?){
        for (i in 0 until multiPart.count) {

            val part = multiPart.getBodyPart(i)
            kweb.logger.info("Processing multipart $i / ${multiPart.count} of type ${part.disposition}")

            when (val content = part.content){
                is String->{
                    kweb.logger.info("Received content of type string")
                }
                is InputStream ->{
                    kweb.logger.info("Received content of type inputStream. ${part.fileName}")
                    if(part.fileName?.toLowerCase()?.endsWith(".pdf") == true) {
                        kweb.logger.info("Importing attachment ${part.fileName}")
                        processInputStream(part.fileName, content, fromAddress)
                    }
                }
                is Message -> {
                    kweb.logger.info("Received content of type Message")
                    processMessage(content, fromAddress)
                }
                is Multipart -> {
                    kweb.logger.info("Received content of type Multipart")
                    processMultipart(content, fromAddress)
                }
            }
        }
    }

    private fun DocumentId?.isOwnedByThisMaching() = this?.hostname == dbContext.currentConfig.hostname
    private fun DocumentId?.isOwnedByOtherMachine() = this != null && this.hostname != dbContext.currentConfig.hostname
    private fun DocumentId?.isNotPresent() = this != null && this.hostname == dbContext.currentConfig.hostname

    private fun processInputStream(fileName: String, stream: InputStream, fromAddress: String?){
        val documentData = Base64.getEncoder().encodeToString(stream.readBytes())

        val doctagId = extractDocumentIdOrNull(documentData)
        kweb.logger.info("Found doctag -> ${doctagId?.fullUrl ?: "none"}")

        if(doctagId != null && dbContext.documents.countDocuments(Document::url eq doctagId.fullUrl) != 0L){
            kweb.logger.info("Document already imported. Skipping  $doctagId")
            return
        }

        val fd = FileData(_id = documentData.toSha1HexString(), name = fileName, base64Content = documentData, contentType = "application/pdf")
        fd.apply { dbContext.files.save(fd) }



        when{
            doctagId.isOwnedByThisMaching()-> {
                kweb.logger.info("Importing document which is owned by this host.")
                val doc = Document()
                doc._id = doctagId?.id
                doc.url = doctagId?.fullUrl

                doc.attachmentId = fd._id
                doc.attachmentHash = fd.base64Content?.toSha1HexString()
                doc.originalFileName = fileName
                doc.created = ZonedDateTime.now()
                doc.fullText = fd.base64Content?.let { extractTextFromPdf(it) }


                doc.apply { dbContext.documents.save(doc) }
            }
            doctagId.isOwnedByOtherMachine() -> {
                kweb.logger.info("Importing document which is not owned by this host.")

                val docSignRequest = DocumentSignRequest(
                        doctagUrl = doctagId?.fullUrl,
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
                kweb.logger.info("Importing document where no doctag is present")
            }
            else ->  {
                kweb.logger.info("Unknown case")
            }
        }
    }
}