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
import org.bson.internal.Base64
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
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

            call.respond(HttpStatusCode.OK, doc)
        }

        post("/d/{documentId}"){
            logger.info("POST to /d/{documentId}")

            val docId = call.parameters["documentId"]

            val rawSignature = String(call.receiveStream().readAllBytes(), Charsets.UTF_8)
            val signedMessage = DoctagSignature.load(rawSignature)

            if(!signedMessage.valid){
                logger.info("Signature is not valid")
                throw BadRequest("Signature check failed. ${signedMessage.message}")
            }
            logger.info("Signature is valid")

            val doc = docId?.let { db().documents.findOneById(docId) } ?: throw NotFound("Document with id ${docId}")

            if(signedMessage.signedMessage?.documentUrl != doc.url){
                throw BadRequest("Document URL in signature does not match document url of this document. Rejecting signature. ${signedMessage.signedMessage?.documentUrl} != ${doc.url}")
            }

            val sig = Signature(
                signedMessage.signedMessage!!,
                signedMessage.publicKey!!,
                ZonedDateTime.now(),
                rawSignature
            )

            doc.signatures = (doc.signatures?:listOf()).plus(sig)
            db().documents.save(doc)

            call.respond(HttpStatusCode.OK, doc)
        }
    }
}