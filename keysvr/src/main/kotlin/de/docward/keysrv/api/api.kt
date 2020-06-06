package de.docward.keysrv.api

import com.github.salomonbrys.kotson.fromJson
import de.docward.keysrv.BadRequest
import de.docward.keysrv.loadPublicKey
import de.docward.keysrv.model.DbContext
import de.docward.keysrv.model.PublicKeyEntry
import de.docward.keysrv.publicKeyFingerprint
import de.docward.keysrv.verifySignature
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.request.receiveStream
import io.ktor.request.receiveText
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kweb.gson
import org.bson.internal.Base64
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import java.time.ZonedDateTime

fun Routing.publicKeys(){
    get("/pk/{signingParty}/{fingerprint}"){
        val signingParty = call.parameters["signingParty"]
        val fingerprint = call.parameters["fingerprint"]

        val entry = DbContext.publicKeys.findOne(
            and(
                PublicKeyEntry::signingParty eq signingParty,
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

    post("/pk/{signingParty}"){
        val signingParty = call.parameters["signingParty"]

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
        rawEntry.signingParty = signingParty

        val dbEntry = DbContext.publicKeys.findOne(
            and(
                PublicKeyEntry::signingParty eq rawEntry.signingParty,
                PublicKeyEntry::fingerpint eq rawEntry.fingerpint
            )
        )
        if(dbEntry == null){
            DbContext.publicKeys.insertOne(rawEntry)
        }

        call.respond(HttpStatusCode.OK, rawEntry)
    }
}