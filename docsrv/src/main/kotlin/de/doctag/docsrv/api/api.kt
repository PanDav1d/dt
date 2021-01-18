package de.doctag.docsrv.api

import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.lib.DoctagSignature
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.host
import io.ktor.request.receiveStream
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.*
import kweb.logger
import kweb.util.gson
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.bson.internal.Base64
import org.litote.kmongo.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.ZonedDateTime


fun Routing.docsrvApi(){
    get("/health"){
        val isHealthy = db().config.findOne(DocsrvConfig::_id eq "1") != null
        val statusCode = if(isHealthy) HttpStatusCode.OK else HttpStatusCode.InternalServerError

        call.respond(statusCode, HealthCheckResponse(isHealthy))
    }

    get("/discovery"){
        val config = db().currentConfig

        call.respond(HttpStatusCode.OK, DiscoveryResponse("123"))
    }

    get("/d/{documentId}/download"){
        val docId = call.parameters["documentId"]
        val doc = docId?.let{db(call.request.host()).documents.findOneById(docId)}
        val fd = doc?.attachmentId?.let{db().files.findOneById(it)}

        fd?.let { fd ->
            call.response.header("Content-Disposition", """attachment; filename="${doc.originalFileName}"""")
            call.respondBytes(Base64.decode(fd.base64Content),ContentType.parse(fd.contentType!!))
        } ?: call.respond(HttpStatusCode.NotFound, "No document found with id $docId")
    }

    get("/d/{documentId}/viewSignSheet"){
        val docId = call.parameters["documentId"]
        val doc = docId?.let{db(call.request.host()).documents.findOneById(docId)}

        val docToSign = db().files.findOneById(doc?.attachmentId!!)

        val renderer = doc?.let{PdfBuilder(doc, db())}

        renderer?.let { fd ->
            //call.response.header("Content-Disposition", """attachment; filename="${doc.originalFileName}"""")

            val signaturePage = renderer.render().toByteArray()

            val merger = PDFMergerUtility()
            merger.addSource(ByteArrayInputStream(java.util.Base64.getDecoder().decode(docToSign?.base64Content)))
            merger.addSource(ByteArrayInputStream(signaturePage))

            val output = ByteArrayOutputStream()
            merger.destinationStream = output
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())

            call.respondBytes(output.toByteArray(), ContentType.parse("application/pdf"))
        } ?: call.respond(HttpStatusCode.NotFound, "No document found with id $docId")
    }

    get("/f/{fileId}/view"){
        val fileId = call.parameters["fileId"]
        val fileData = fileId?.let{db(call.request.host()).files.findOneById(fileId)}

        fileData?.let { fd ->
            call.respondBytes(Base64.decode(fd.base64Content),ContentType.parse(fd.contentType!!))
        } ?: call.respond(HttpStatusCode.NotFound, "No file found with id $fileId")
    }

    get("/f/{fileId}/download"){
        val fileId = call.parameters["fileId"]
        val fileData = fileId?.let{db(call.request.host()).files.findOneById(fileId)}

        fileData?.let { fd ->
            call.response.header("Content-Disposition", """attachment; filename="${fileData.name}"""")
            call.respondBytes(Base64.decode(fd.base64Content),ContentType.parse(fd.contentType!!))
        } ?: call.respond(HttpStatusCode.NotFound, "No file found with id $fileId")
    }


    acceptExcludingWildcards(ContentType.Application.Json) {
        get("/d/{documentId}") {
            val docId = call.parameters["documentId"]
            val doc = docId?.let { db().documents.findOne(Document::url eq "https://${db().currentConfig.hostname}/d/${docId}") } ?: throw NotFound("Document with id ${docId}")
            //val signature = call.request.header("X-Message-Signature") ?: throw BadRequest("No X-Message-Signature Header found. Requesting party can't be authenticated. Won't reply.")

            call.respond(HttpStatusCode.OK, doc.toEmbeddedDocument(db()))
        }

        post("/d/{documentId}/{hostname}"){
            logger.info("POST to /d/{documentId}/{hostName}")

            val docId = call.parameters["documentId"]
            val hostName = call.parameters["hostname"]

            val rawSignature = String(call.receiveStream().readAllBytes(), Charsets.UTF_8)
            val signedMessage = EmbeddedSignature.load(rawSignature)



            logger.info("Signature is valid")

            val doc = docId?.let { db().documents.findOne(Document::url eq "https://$hostName/d/$docId") } ?: throw NotFound("Document with id ${docId}")

            if(signedMessage.signature.doc?.documentUrl != doc.url){
                throw BadRequest("Document URL in signature does not match document url of this document. Rejecting signature. ${signedMessage.signature.doc?.documentUrl} != ${doc.url}")
            }


            signedMessage.files.forEach {
                db().files.save(it)
            }

            doc.signatures = (doc.signatures?:listOf()).plus(signedMessage.signature)
            db().documents.save(doc)

            call.respond(HttpStatusCode.OK, doc)
        }
    }
}