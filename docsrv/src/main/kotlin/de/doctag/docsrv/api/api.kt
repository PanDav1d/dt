package de.doctag.docsrv.api

import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.lib.*
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.model.PublicKeyVerification
import de.doctag.lib.model.PublicKeyVerificationResult
import documentWasSignedMail
import ensureUserIsAuthenticated
import io.ktor.application.*
import ktor.swagger.ok
import ktor.swagger.responds
import ktor.swagger.get
import ktor.swagger.put
import ktor.swagger.post
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ktor.swagger.operationId
import ktor.swagger.version.shared.Group
import kweb.logger
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.bson.internal.Base64
import org.litote.kmongo.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.ZonedDateTime
import java.util.*


fun Routing.docServerApi(){
    @Group("DocServer")
    @Location("/health")
    class HealthCheckRequestPath
    get<HealthCheckRequestPath>(
        "Perform Health Check".responds(
            ok<HealthCheckResponse>()
        ).operationId("checkHealth")
    ) {
            req ->

        val isHealthy = db().config.findOne(DocsrvConfig::_id eq "1") != null

        if(isHealthy){
            call.respond(HttpStatusCode.OK, HealthCheckResponse(isHealthy))
        } else{
            throw Exception("Failed to connect to db")
        }
    }


    @Group("DocServer")
    @Location("/discovery")
    class DiscoveryRequestPath
    get<DiscoveryRequestPath>(
        "Perform Instance discovery".responds(
            ok<DiscoveryResponse>()
        ).operationId("discoverInstance")
    ) { req ->
        call.respond(HttpStatusCode.OK, DiscoveryResponse("HELLO"))
    }


    @Group("DocServer")
    @Location("/k/{publicKeyFingerprint}/verify/{seed}")
    class VerifyInstancePrivateKeyRequestPath(val publicKeyFingerprint: String, val seed: String)
    get<VerifyInstancePrivateKeyRequestPath>(
        "Check that this instance actually owns the given private key".responds(
            ok<DiscoveryResponse>()
        ).operationId("verifyInstanceHasPrivateKey")
    ) { req ->

        val publicKeyFingerprint = req.publicKeyFingerprint
        val seed = req.seed
        val seed2 = generateRandomString(1024)

        val ppk = db().keys.findOne(PrivatePublicKeyPair::fingerprint eq publicKeyFingerprint)
        val privKey = ppk?.privateKey?.let{loadPrivateKey(it)}
        if(privKey != null){
            val msg = "$seed$$seed2"
            val signature = makeSignature(privKey, msg)

            val result = PublicKeyVerificationResult(msg, signature)
            call.respond(HttpStatusCode.OK, result)
        }
        else {
            throw NotFoundException("Public Key is unknown")
        }
    }


    @Group("DocServer")
    @Location("/k/{publicKeyFingerprint}/verification")
    class ReceiveKeyVerificationRequestPath(val publicKeyFingerprint: String)
    put<ReceiveKeyVerificationRequestPath, PublicKeyVerification>(
        "Set the verification of the private public key".responds(
            ok<DiscoveryResponse>()
        ).operationId("setVerificationOfKeyPair")
    ) { req, verification ->

        val publicKeyFingerprint = req.publicKeyFingerprint

        val ppk = db().keys.findOne(PrivatePublicKeyPair::fingerprint eq publicKeyFingerprint)

        if(ppk != null) {
            ppk.verification = verification
            if(ppk.verifySignature()){
                db().keys.save(ppk)
                call.respond(HttpStatusCode.OK, PublicKeyVerificationResponse())
            } else {
                throw BadRequestException("Signature verification failed")
            }
        } else {
            throw NotFoundException("Public Key is unknown")
        }
    }

    @Group("DocServer")
    @Location("/d/")
    class AddDocumentRequestPath
    post<AddDocumentRequestPath, DocumentToAdd>("Add Document".responds(ok<DocumentAddResponse>()).operationId("addDocument"))
    { req, postData->
        ensureUserIsAuthenticated()

        logger.info("Received ")

        val workflow = if(postData.workflow != null) {
            db().workflows.findOne(Workflow::name eq postData.workflow) ?: throw BadRequestException("Workflow not found")
        } else {
            val initial = db().currentConfig.workflow?.defaultWorkflowId
            initial?.let {
                db().workflows.findOneById(initial)
            }
        }

        val fd = FileData()
        fd.name = postData.fileName
        fd.contentType = "application/pdf"
        fd.base64Content = postData.data


        val docId = try {
            extractDocumentIds(postData.data)?.firstOrNull()?.documentId
        } catch(ex: java.lang.Exception){
            logger.error(ex)
            logger.error("Failed to extract document id. Assume no document id is present")

            null
        }.let {
            if(it == null){
                val randomDoctag = "https://${db().currentConfig.hostname}/d/${generateRandomString(16)}"
                fd.base64Content = insertDoctagIntoPDF(postData.data, randomDoctag, postData.doctagPosX ?: 0.885f, postData.doctagPosY ?: 0.08f, postData.doctagSize ?: 10.0f)
                DocumentId.parse(randomDoctag)
            } else {
                it
            }
        }

        if(docId.hostname != db().currentConfig.hostname){
            throw BadRequestException("Document has unexpected hostname. Expected ${db().currentConfig.hostname}. But was ${docId.hostname}")
        }

        fd._id = fd.base64Content!!.toSha1HexString()

        val docObj = Document()
        docObj.url = docId.fullUrl
        docObj.attachmentId = fd._id
        docObj.attachmentHash = fd.base64Content?.toSha1HexString()
        docObj.created = ZonedDateTime.now()
        docObj.originalFileName = fd.name
        docObj.fullText = fd.base64Content?.let { extractTextFromPdf(it) }
        docObj.workflow = workflow
        docObj.tags = docObj.fullText.determineMatchingTags(db().tags.find().toList())

        db().files.save(fd)
        db().documents.save(docObj)

        logger.info("Added document with url ${docId.fullUrl}")

        call.respond(HttpStatusCode.OK,DocumentAddResponse(documentUrl = docId.fullUrl))
    }


    @Group("DocServer")
    @Location("/d/{documentId}/download")
    class DownloadDocumentRequestPath(val documentId: String)
    get<DownloadDocumentRequestPath>(
        "Download document".responds(
            ok<DiscoveryResponse>()
        ).operationId("downloadDocument")
    ) { req ->

        val doc = db().documents.findOneById(req.documentId)
        val fd = doc?.attachmentId?.let { db().files.findOneById(it) }

        fd?.let {
            call.respondBytes(Base64.decode(fd.base64Content), status = HttpStatusCode.OK)
        } ?: throw NotFoundException("Document with id ${req.documentId} not found")
    }


    @Group("DocServer")
    @Location("/d/{documentId}/viewSignSheet")
    class DownloadSignSheetRequestPath(val documentId: String)
    get<DownloadSignSheetRequestPath>(
        "Download sign sheet".responds(
            ok<DiscoveryResponse>()
        ).operationId("downloadSignSheet")
    ) { req ->

        val doc = db().documents.findOneById(req.documentId) ?: throw NotFoundException("No document found with id ${req.documentId}")

        val docToSign = db().files.findOneById(doc.attachmentId!!)

        val renderer = PdfBuilder(doc, db(), language = Locale.GERMANY)

        renderer.let { fd ->
            val signaturePage = renderer.render().toByteArray()

            val merger = PDFMergerUtility()
            merger.addSource(ByteArrayInputStream(java.util.Base64.getDecoder().decode(docToSign?.base64Content)))
            merger.addSource(ByteArrayInputStream(signaturePage))

            val output = ByteArrayOutputStream()
            merger.destinationStream = output
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())

            val outputDoc = output.toByteArray()
            val pdf = PDDocument.load(outputDoc)
            pdf.enableProtection()

            val finalOutput = ByteArrayOutputStream()
            pdf.save(finalOutput)

            call.respondBytes(finalOutput.toByteArray(), status = HttpStatusCode.OK)
        }
    }

    acceptExcludingWildcards(ContentType.Application.Json){
        @Group("DocServer")
        @Location("/d/{documentId}")
        class FetchDoctagDocumentRequestPath(val documentId: String)
        get<FetchDoctagDocumentRequestPath>(
            "Fetch doctag document".responds(
                ok<EmbeddedDocument>()
            ).operationId("fetchDoctagDocument")
        ) { req ->
            val docId = req.documentId

            val documentUrlToSearch = "https://${db().currentConfig.hostname}/d/${docId}"

            val doc = db().documents.findOne(
                Document::url eq documentUrlToSearch
            ) ?: throw NotFoundException("Document with id $docId")

            call.respond(doc.toEmbeddedDocument(db()))
        }


        @Group("DocServer")
        @Location("/d/{documentId}/{hostname}")
        class SubmitSignatureRequestPath(val documentId: String, val hostname: String)
        post<SubmitSignatureRequestPath, EmbeddedSignature>(
            "Add signature to document".responds(
                ok<Document>()
            ).operationId("addSignatureToDoctagDocument")
        ) { req, signedMessage  ->

            val givenDocumentUrl = "https://${req.hostname}/d/${req.documentId}"

            logger.info("addSignatureToDoctagDocument called for ${givenDocumentUrl}")
            logger.info("Signature is loaded")
            logger.info("Is signature valid? ${signedMessage.signature.isValid()}")
            logger.info("Is signing key verified? ${signedMessage.signature.signedByKey?.verifySignature()}")

            val doc = db().documents.findOne(Document::url eq givenDocumentUrl)
                ?: throw NotFoundException("Document with id ${req.documentId}")

            if(signedMessage.signature.data?.documentUrl != doc.url){
                throw BadRequest("Document URL in signature does not match document url of this document. Rejecting signature. ${signedMessage.signature.data?.documentUrl} != ${doc.url}")
            }

            signedMessage.files.forEach {
                db().files.save(it)
            }

            doc.signatures = (doc.signatures?:listOf()).plus(signedMessage.signature)
            db().documents.save(doc)

            val distributeToUrls = doc.signatures?.map { it.data?.signingDoctagInstance }?.distinct()
            distributeToUrls?.filterNotNull()?.filter{!doc.url!!.contains(it)}?.forEach { url ->
                try{
                    withContext(Dispatchers.IO){
                        DocServerClient.notifyDocumentDidChange(url, doc.url!!)
                    }
                }
                catch(ex: Exception){
                    logger.error(ex.message)
                }
            }

            val distributeToMailReceiverFields = doc.workflow?.actions?.flatMap { it.inputs?.filter { it.kind == WorkflowInputKind.ReceiptMail }?.map { it.name } ?: listOf() } ?: listOf()
            val distributeToMails = doc.signatures?.flatMap { it.inputs?.filter { it.name in distributeToMailReceiverFields }?.map { it.value } ?: listOf() }

            logger.info("Sending info mail to ${distributeToMails}")

            distributeToMails?.filterNotNull()?.distinct()?.forEach { mail->
                db().currentConfig.outboundMail?.let{
                    documentWasSignedMail(
                        it,
                        mail,
                        doc.url!!,
                        Locale.GERMANY
                    )
                }
            }

            call.respond(HttpStatusCode.OK, doc)
        }

        @Group("DocServer")
        @Location("/d/notifyChanges/")
        class RefreshDocumentRequestPath()
        post<RefreshDocumentRequestPath, NotifyRequest>(
            "Add signature to document".responds(
                ok<NotifyResult>()
            ).operationId("notifyChangesOfDoctagDocument")
        ) { req, notifyRequest  ->

            val doc = withContext(Dispatchers.IO){
                DocServerClient.loadDocument(notifyRequest.url)
            }

            doc?.files?.forEach {
                db().files.save(it)
            }
            doc?.document?.let {
                val currentDoc = db().documents.findOne(Document::_id eq it._id)

                // Preserve Tags from current document
                if(currentDoc != null)
                    it.tags = currentDoc?.tags
                else
                    it.tags = it.fullText.determineMatchingTags(db().tags.find().toList())


                db().documents.save(it)
            }

            call.respond(HttpStatusCode.OK, NotifyResult())
        }
    }

    @Group("DocServer")
    @Location("/f/{fileId}/view")
    class ViewFileRequestPath(val fileId: String)
    get<ViewFileRequestPath>(
        "View a document with the given ID hosted on this instance".responds(
            ok<DiscoveryResponse>()
        ).operationId("viewFile")
    ) { req ->

        val fileData = db().files.findOneById(req.fileId) ?: throw NotFoundException("No file found with id ${req.fileId}")

        call.respondBytes(status = HttpStatusCode.OK, bytes=Base64.decode(fileData.base64Content))

    }

    @Group("DocServer")
    @Location("/f/{fileId}/download")
    class DownloadFileRequestPath(val fileId: String)
    get<DownloadFileRequestPath>(
        "Download a document with the given ID hosted on this instance".responds(
            ok<DiscoveryResponse>()
        ).operationId("downloadFile")
    ) { req ->
        val fileData = db().files.findOneById(req.fileId) ?: throw NotFoundException("No file found with id ${req.fileId}")

        context.response.header("Content-Disposition", """attachment; filename="${fileData.name}"""")
        call.respondBytes(status=HttpStatusCode.OK, bytes=Base64.decode(fileData.base64Content))
    }
}


class NotifyRequest(val url: String)
class NotifyResult()

class NotFoundException(val msg: String): Exception()
class BadRequestException(val msg: String): Exception()
class PublicKeyVerificationResponse()