package de.doctag.docsrv.api

import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.lib.fixHttps
import de.doctag.lib.generateRandomString
import de.doctag.lib.loadPrivateKey
import de.doctag.lib.makeSignature
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.model.PublicKeyVerification
import de.doctag.lib.model.PublicKeyVerificationResult
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
    @Location("/k/{publicKeyFingerprint}/verify/{seed}")
    class ReceiveKeyVerificationRequestPath(val publicKeyFingerprint: String, val seed: String)
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

        val renderer = PdfBuilder(doc, db())

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
            logger.info("Documents in db ${db().documents.find().map { it.url }.toList()}. Searching for $documentUrlToSearch")

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

            call.respond(HttpStatusCode.OK, doc)
        }
    }

    @Group("DocServer")
    @Location("/f/{fileId}/view")
    class ViewFileRequestPath(val fileId: String)
    get<ViewFileRequestPath>(
        "Perform Instance discovery".responds(
            ok<DiscoveryResponse>()
        ).operationId("viewFile")
    ) { req ->

        val fileData = db().files.findOneById(req.fileId) ?: throw NotFoundException("No file found with id ${req.fileId}")

        call.respond(Base64.decode(fileData.base64Content))

    }

    @Group("DocServer")
    @Location("/f/{fileId}/download")
    class DownloadFileRequestPath(val fileId: String)
    get<DownloadFileRequestPath>(
        "Perform Instance discovery".responds(
            ok<DiscoveryResponse>()
        ).operationId("downloadFile")
    ) { req ->
        val fileData = db().files.findOneById(req.fileId) ?: throw NotFoundException("No file found with id ${req.fileId}")

        context.response.header("Content-Disposition", """attachment; filename="${fileData.name}"""")
        call.respond(Base64.decode(fileData.base64Content).inputStream())
    }
}



class NotFoundException(val msg: String): Exception()

class BadRequestException(val msg: String): Exception()

class PublicKeyVerificationResponse()