package de.doctag.docsrv.api

import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.content.type.binary.BinaryRequest
import com.papsign.ktor.openapigen.content.type.binary.BinaryResponse
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.path.normal.put
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.throws
import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.lib.generateRandomString
import de.doctag.lib.loadPrivateKey
import de.doctag.lib.makeSignature
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.model.PublicKeyVerification
import de.doctag.lib.model.PublicKeyVerificationResult
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.*
import kweb.logger
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.bson.internal.Base64
import org.litote.kmongo.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


@Path("{documentId}/download")
data class DocumentDownloadRequest(
    @PathParam("ID of the document to download") val documentId: String
)

@Path("{publicKeyFingerprint}/verify/{seed}")
data class VerifyPrivateKeyRequest(
    @PathParam("Fingerprint of the requested public key") val publicKeyFingerprint: String,
    @PathParam("Seed that shall be used to prove the existence of the private ky") val seed: String
)

@Path("{publicKeyFingerprint}/verification")
data class ReceivePrivateKeyVerificationRequest(
    @PathParam("Fingerprint of the requested public key") val publicKeyFingerprint: String
)

@Path("{documentId}/viewSignSheet")
data class ViewSignSheetReqeuest(
    @PathParam("ID of the document to download") val documentId: String
)

@Path("{documentId}")
data class FetchDoctagDocumentRequest(
    @PathParam("ID of the document to fetch") val documentId: String
)

@Path("{documentId}/{hostname}")
data class PostDoctagDocumentRequest(
    @PathParam("ID of the document to download") val documentId: String,
    @PathParam("ID of the document to download") val hostname: String
)

@Path("{fileId}/view")
data class ViewFileRequest(
    @PathParam("ID of the document to download") val fileId: String
)

@Path("{fileId}/download")
data class DownloadFileRequest(
    @PathParam("ID of the document to download") val fileId: String
)

fun NormalOpenAPIRoute.docsrvApi2(){
    route("health"){
        throws(HttpStatusCode.InternalServerError, "Something went wrong", {ex: Exception -> ex.toString()}) {
            get<Unit, HealthCheckResponse>(
                id("checkHealth")
            ){
                val isHealthy = pipeline.db().config.findOne(DocsrvConfig::_id eq "1") != null

                if(isHealthy){
                    respond(HealthCheckResponse(isHealthy))
                } else{
                    throw Exception("Failed to connect to db")
                }
            }
        }
    }

    route("discovery") {
        get<Unit, DiscoveryResponse>(id("discoverInstance")){
            respond(DiscoveryResponse("HELLO"))
        }
    }

    route("k"){
        throws(HttpStatusCode.NotFound, "Public Key is unknown", {ex: NotFoundException->ex.msg}) {
            throws(HttpStatusCode.BadRequest, "Input data is invalid",{ex: BadRequestException -> ex.msg}) {

                get<VerifyPrivateKeyRequest,PublicKeyVerificationResult>(id("verifyInstanceHasPrivateKey")){ request ->
                    val publicKeyFingerprint = request.publicKeyFingerprint
                    val seed = request.seed
                    val seed2 = generateRandomString(1024)

                    val ppk = pipeline.db().keys.findOne(PrivatePublicKeyPair::fingerprint eq publicKeyFingerprint)
                    val privKey = ppk?.privateKey?.let{loadPrivateKey(it)}
                    if(privKey != null){
                        val msg = "$seed$$seed2"
                        val signature = makeSignature(privKey, msg)

                        val result = PublicKeyVerificationResult(msg, signature)
                        respond(result)
                    }
                    else {
                        throw NotFoundException("Public Key is unknown")
                    }
                }

                put<ReceivePrivateKeyVerificationRequest, PublicKeyVerificationResponse, PublicKeyVerification>(id("setVerificationOfKeyPair")){ params, verification ->
                    val publicKeyFingerprint = params.publicKeyFingerprint

                    val ppk = pipeline.db().keys.findOne(PrivatePublicKeyPair::fingerprint eq publicKeyFingerprint)

                    if(ppk != null) {
                        ppk.verification = verification
                        if(ppk.verifySignature()){
                            pipeline.db().keys.save(ppk)
                            respond(PublicKeyVerificationResponse())
                        } else {
                            throw BadRequestException("Signature verification failed")
                        }
                    } else {
                        throw NotFoundException("Public Key is unknown")
                    }
                }
            }
        }
    }

    route("d"){
        throws(HttpStatusCode.NotFound, "Object does not exist", {ex: NotFoundException->ex.msg}) {

            get<DocumentDownloadRequest, RawPdf>(id("downloadDocument")) { params ->
                val doc = pipeline.db().documents.findOneById(params.documentId)
                val fd = doc?.attachmentId?.let { pipeline.db().files.findOneById(it) }

                fd?.let {
                    respond(RawPdf(Base64.decode(fd.base64Content).inputStream()))
                } ?: throw NotFoundException("Document with id ${params.documentId} not found")
            }


            get<ViewSignSheetReqeuest, RawPdf>(id("downloadSignSheet")){ params ->
                val doc = pipeline.db().documents.findOneById(params.documentId) ?: throw NotFoundException("No document found with id ${params.documentId}")

                val docToSign = pipeline.db().files.findOneById(doc.attachmentId!!)

                val renderer = PdfBuilder(doc, pipeline.db())

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

                    respond(RawPdf(finalOutput.toByteArray().inputStream()))
                }
            }

            acceptExcludingWildcards(ContentType.Application.Json){

                get<FetchDoctagDocumentRequest, EmbeddedDocument>(id("fetchDoctagDocument")){ params->
                    val docId = params.documentId
                    val doc = pipeline.db().documents.findOne(
                        Document::url eq "https://${pipeline.db().currentConfig.hostname}/d/${docId}"
                    ) ?: throw NotFoundException("Document with id $docId")

                    respond(doc.toEmbeddedDocument(pipeline.db()))
                }

                post<PostDoctagDocumentRequest, Document, RawBytesRequest>(id("addSignatureToDoctagDocument")){ params, input ->
                    val rawSignature = String(input.stream.readAllBytes(), Charsets.UTF_8)
                    val signedMessage = EmbeddedSignature.load(rawSignature)

                    logger.info("Signature is loaded")
                    logger.info("Is signature valid? ${signedMessage.signature.isValid()}")
                    logger.info("Is signing key verified? ${signedMessage.signature.signedByKey?.verifySignature()}")

                    val doc = pipeline.db().documents.findOne(Document::url eq "https://${params.hostname}/d/${params.documentId}")
                        ?: throw NotFoundException("Document with id ${params.documentId}")

                    if(signedMessage.signature.data?.documentUrl != doc.url){
                        throw BadRequest("Document URL in signature does not match document url of this document. Rejecting signature. ${signedMessage.signature.data?.documentUrl} != ${doc.url}")
                    }

                    signedMessage.files.forEach {
                        pipeline.db().files.save(it)
                    }

                    doc.signatures = (doc.signatures?:listOf()).plus(signedMessage.signature)
                    pipeline.db().documents.save(doc)

                    respond(doc)
                }
            }
        }
    }

    route("f"){
        throws(HttpStatusCode.NotFound, "Object does not exist", {ex: NotFoundException->ex.msg}) {

            get<ViewFileRequest, RawPdf>(id("viewFile")){params ->
                val fileData = pipeline.db().files.findOneById(params.fileId) ?: throw NotFoundException("No file found with id ${params.fileId}")

                respond(RawPdf(Base64.decode(fileData.base64Content).inputStream()))
            }

            get<DownloadFileRequest, RawPdf>(id("downloadFile")){params ->
                val fileData = pipeline.db().files.findOneById(params.fileId) ?: throw NotFoundException("No file found with id ${params.fileId}")

                pipeline.context.response.header("Content-Disposition", """attachment; filename="${fileData.name}"""")
                respond(RawPdf(Base64.decode(fileData.base64Content).inputStream()))
            }

        }
    }
}


@BinaryRequest(["octet/stream"])
data class RawBytesRequest(val stream: InputStream)

@BinaryResponse(["application/pdf"])
data class RawPdf(val stream: InputStream)

class NotFoundException(val msg: String): Exception()

class BadRequestException(val msg: String): Exception()

class PublicKeyVerificationResponse()