package de.doctag.docsrv.api

import de.doctag.docsrv.*
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.Signature
import de.doctag.lib.DoctagSignature
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receiveStream
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.*
import kweb.gson
import kweb.logger
import org.bson.internal.Base64
import org.litote.kmongo.findOneById
import java.time.ZonedDateTime


fun Routing.downloadAttachment(){
    get("/d/{documentId}/download"){
        val docId = call.parameters["documentId"]
        val doc = docId?.let{DbContext.documents.findOneById(docId)}
        val fd = doc?.attachmentId?.let{DbContext.files.findOneById(it)}

        fd?.let { fd ->
            call.response.header("Content-Disposition", """attachment; filename="${doc.originalFileName}"""")
            call.respondBytes(Base64.decode(fd.base64Content),ContentType.parse(fd.contentType!!))
        } ?: call.respond(HttpStatusCode.NotFound, "No document found with id $docId")
    }


    acceptExcludingWildcards(ContentType.Application.Json) {
            get("/d/{documentId}") {
                val docId = call.parameters["documentId"]
                val doc = docId?.let { DbContext.documents.findOneById(docId) } ?: throw NotFound("Document with id ${docId}")
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

                val doc = docId?.let { DbContext.documents.findOneById(docId) } ?: throw NotFound("Document with id ${docId}")

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

                call.respond(HttpStatusCode.OK, doc)
            }
        }
}