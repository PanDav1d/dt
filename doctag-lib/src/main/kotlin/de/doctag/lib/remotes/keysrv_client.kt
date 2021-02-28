package de.doctag.lib

import de.doctag.lib.model.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.ZonedDateTime


var BASE_URL = "https://keyserver.doctag.de/"

val logger = LoggerFactory.getLogger("de.doctag.lib.keysrv_client");



data class PublicKeyAddRequest(
    var publicKey : String? = null,
    val verboseName: String? = null,
    var owner: Person = Person(),
    var ownerAddress: Address = Address(),
    var signingDoctagInstance: String? = null
)

data class PublicKeyResponse(
    override var publicKey : String? = null,
    override val verboseName: String? = null,
    override var created: String? = null,
    override var owner: Person = Person(),
    override var ownerAddress: Address = Address(),
    override var signingDoctagInstance: String? = null,
    override var verification: PublicKeyVerification? = null
): BasePublicKeyEntry(publicKey, verboseName, created, owner, ownerAddress, signingDoctagInstance, verification)


fun PrivatePublicKeyPair.toPublicKeyAddRequest() : PublicKeyAddRequest {
    return PublicKeyAddRequest(
            this.publicKey,
            this.verboseName,
            //this.created,
            this.owner,
            this.ownerAddress,
            this.signingDoctagInstance
    )
}

class KeyServerException(msg: String) : Exception(msg)

object KeyServerClient {

    val client: HttpClient by lazy{
        HttpClient.newBuilder().build()
    }

    fun loadPublicKey(signingDoctagInstance: String, fingerprint: String): Triple<Boolean, PublicKeyResponse?, String?> {
        val targetUrl = BASE_URL +"/pk/${signingDoctagInstance}/${fingerprint}"

        val request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMinutes(1))
                .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("fetching public key for ${signingDoctagInstance}/${fingerprint}")

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
        logger.info("called publishPublicKey")

        val publicKeyReq = ppk.toPublicKeyAddRequest()

        val targetUrl = "$BASE_URL/pk/"

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