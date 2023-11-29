package de.doctag.keysrv.api

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.fromJson
import de.doctag.keysrv.BadRequest
import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.PublicKeyEntry
import de.doctag.lib.*
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
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


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

    post("/pk/"){

        val rawText = String(call.receiveStream().readAllBytes(), Charsets.UTF_8)

        if(rawText.length > 4096){
            throw BadRequest("Request size exceeded. Max allowed: 4096. But was ${rawText.length}")
        }

        val rawEntry = getJackson().readValue<PublicKeyEntry>(rawText) //gson.fromJson<PublicKeyEntry>(rawText)

        // Todo: Verify if signingDoctagInstance is reachable and really owns the private key which belongs to the public key

        val signature = call.request.header("X-Message-Signature") ?: throw BadRequest("No X-Message-Signature Header found")
        val pk = loadPublicKey(rawEntry.publicKey) ?: throw BadRequest("Failed to load Public key")

        if(!verifySignature(pk, rawText, signature)){
            throw BadRequest("Failed to verify signature")
        }

        rawEntry._id = null
        rawEntry.created = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        rawEntry.fingerpint = publicKeyFingerprint(pk)

        val dbEntry = DbContext.publicKeys.findOne(
            and(
                PublicKeyEntry::signingDoctagInstance eq rawEntry.signingDoctagInstance,
                PublicKeyEntry::fingerpint eq rawEntry.fingerpint
            )
        )
        if(dbEntry == null){
            DbContext.publicKeys.insertOne(rawEntry)
        }

        thread(start=true){
            try {
                val email = EmailContent(
                    greeting = "Hallo",
                    text = """Ein neuer Key wurde angelegt. Bitte auf Keyserver einloggen und prüfen""".trimMargin(),
                    actionText = "Keyserver",
                    actionUrl = "https://keyserver.doctag.de",
                    byeText = "Viele Grüße "
                )
                MailSender(
                    listOf("hello@doctag.io", "f.englert@gmail.com"),
                    "Key muss signiert werden",
                    email,
                    Config.smtpServer,
                    Config.smtpUser,
                    Config.smtpPassword,
                    Config.fromAddress,
                    ZonedDateTime.now(),
                    SendMailProtocol.SMTP
                )
            }catch(ex:Exception){
                logger.error("Failed to send mail!")
                logger.error(ex.message)
            }
        }

        call.respond(HttpStatusCode.OK, rawEntry.copy(verification = dbEntry?.verification))
    }
}