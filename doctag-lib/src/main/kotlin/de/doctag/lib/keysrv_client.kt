package de.doctag.lib

import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import de.doctag.lib.model.PrivatePublicKeyPair
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration


var BASE_URL = "https://keysrv.englert.xyz"

val logger = LoggerFactory.getLogger("de.doctag.lib.keysrv_client");

data class PublicKeyAddRequest(
        var publicKey : String? = null,
        val verboseName: String? = null,
        var owner: Person = Person(),
        var issuer: Address = Address(),
        var signingParty: String? = null
)

data class PublicKeyResponse(
        var publicKey : String? = null,
        val verboseName: String? = null,
        var owner: Person = Person(),
        var issuer: Address = Address(),
        var signingParty: String? = null
)

fun PrivatePublicKeyPair.toPublicKeyAddRequest() : PublicKeyAddRequest {
    return PublicKeyAddRequest(
            this.publicKey,
            this.verboseName,
            //this.created,
            this.owner,
            this.issuer,
            this.signingParty
    )
}

class KeyServerException(msg: String) : Exception(msg)

object KeyServerClient {

    val client: HttpClient by lazy{
        HttpClient.newBuilder().build()
    }

    fun loadPublicKey(signingParty: String, fingerprint: String): Triple<Boolean, PublicKeyResponse?, String?> {
        val targetUrl = BASE_URL +"/pk/${signingParty}/${fingerprint}"

        val request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMinutes(1))
                .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("fetching public key for ${signingParty}/${fingerprint}")

        return when(resp.statusCode()){
            200 -> {
                val obj = getJackson().readValue(resp.body(), PublicKeyResponse::class.java)
                Triple(true , obj, null)
            }
            else ->{
                logger.info("Failed to fetch public key")
                Triple(false , null, resp.body())
            }
        }
    }

    fun publishPublicKey(ppk: PrivatePublicKeyPair) : Pair<Boolean, String?>{
        val publicKeyReq = ppk.toPublicKeyAddRequest()

        //val targetUrl = "http://127.0.0.1:16098/pk/${ppk.signingParty}"
        val targetUrl = BASE_URL +"/pk/${ppk.signingParty}"

        val data = getJackson().writeValueAsString(publicKeyReq)
        val signature = makeSignature(loadPrivateKey(ppk.privateKey!!)!!, data)

        logger.info("Public key data is ")
        logger.info(data)
        logger.info("Signature is")
        logger.info(signature)
        logger.info("Sigature self check: ${verifySignature(loadPublicKey(ppk.publicKey)!!, data, signature)}")


        val request = HttpRequest.newBuilder()
            .uri(URI.create(targetUrl))
            .timeout(Duration.ofMinutes(1))
            .header("Content-Type", "application/json; charset=utf-8")
            .header("X-Message-Signature", signature)
            .POST(HttpRequest.BodyPublishers.ofString(data, Charsets.UTF_8))
            .build()


        try {
            val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

            logger.info("Response status ${resp.statusCode()}")
            logger.info("Response string ${resp.body()}")


            return if(resp.statusCode() != 200){
                false to "Failed to upload public key: " + resp.body()
            }
            else {
                true to null
            }

        } catch(ex: Exception){
            logger.error("Failed to submit key")
            logger.error(ex.message)

            return false to "Failed to connect"
        }
    }
}