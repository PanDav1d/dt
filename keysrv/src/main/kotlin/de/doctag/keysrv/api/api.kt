package de.doctag.keysrv.api

import com.github.salomonbrys.kotson.fromJson
import de.doctag.keysrv.BadRequest
import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.PublicKeyEntry
import de.doctag.lib.loadPublicKey
import de.doctag.lib.publicKeyFingerprint
import de.doctag.lib.verifySignature
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kweb.util.gson
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.time.ZonedDateTime


class HealthCheckResponse(val healthy:Boolean)

fun Routing.publicKeys(){

    get("/health"){
        call.respond(HttpStatusCode.OK, HealthCheckResponse(healthy = true))
    }

    get("/pk/{signingDoctagInstance}/{fingerprint}"){
        val signingDoctagInstance = call.parameters["signingDoctagInstance"]
        val fingerprint = call.parameters["fingerprint"]

        val entry = DbContext.publicKeys.findOne(
            and(
                PublicKeyEntry::signingDoctagInstance eq signingDoctagInstance,
                PublicKeyEntry::fingerpint eq fingerprint
            )
        )

        if(entry != null){
            call.respond(HttpStatusCode.OK, entry)
        }
        else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    post("/pk/{signingDoctagInstance}"){
        val signingDoctagInstance = call.parameters["signingDoctagInstance"]

        val rawText = String(call.receiveStream().readAllBytes(), Charsets.UTF_8)
        val rawEntry = gson.fromJson<PublicKeyEntry>(rawText)
        val signature = call.request.header("X-Message-Signature") ?: throw BadRequest("No X-Message-Signature Header found")
        val pk = loadPublicKey(rawEntry.publicKey) ?: throw BadRequest("Failed to load Public key")

        if(!verifySignature(pk, rawText, signature)){
            throw BadRequest("Failed to verify signature")
        }

        rawEntry._id = null
        rawEntry.created = ZonedDateTime.now()
        rawEntry.fingerpint = publicKeyFingerprint(pk)
        rawEntry.signingDoctagInstance = signingDoctagInstance

        val dbEntry = DbContext.publicKeys.findOne(
            and(
                PublicKeyEntry::signingDoctagInstance eq rawEntry.signingDoctagInstance,
                PublicKeyEntry::fingerpint eq rawEntry.fingerpint
            )
        )
        if(dbEntry == null){
            DbContext.publicKeys.insertOne(rawEntry)
        }

        call.respond(HttpStatusCode.OK, rawEntry)
    }
}