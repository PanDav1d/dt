package de.docward.docsrv.keysrv_api

import com.github.salomonbrys.kotson.fromJson
import de.docward.docsrv.loadPrivateKey
import de.docward.docsrv.loadPublicKey
import de.docward.docsrv.makeSignature
import de.docward.docsrv.model.Address
import de.docward.docsrv.model.Person
import de.docward.docsrv.model.PrivatePublicKeyPair
import de.docward.docsrv.verifySignature
import kweb.gson
import kweb.logger
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.ZonedDateTime


private val BASE_URL = "https://keysrv.englert.xyz"

data class PublicKeyAddRequest(
    var publicKey : String? = null,
    val verboseName: String? = null,
    //var created: ZonedDateTime? = null,
    var owner: Person = Person(),
    var issuer: Address = Address(),
    var signingParty: String? = null
)

data class PublicKeyResponse(
        var publicKey : String? = null,
        val verboseName: String? = null,
        //var created: ZonedDateTime? = null,
        var owner: Person = Person(),
        var issuer: Address = Address(),
        var signingParty: String? = null
)

fun PrivatePublicKeyPair.toPublicKeyAddRequest() : PublicKeyAddRequest{
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

object KeyServer {

    val client: HttpClient by lazy{
        HttpClient.newBuilder().build()
    }

    fun loadPublicKey(signingParty: String, fingerprint: String): Triple<Boolean, PublicKeyResponse?, String?> {
        val targetUrl = BASE_URL+"/pk/${signingParty}/${fingerprint}"

        val request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMinutes(1))
                .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("fetching public key for ${signingParty}/${fingerprint}")

        return when(resp.statusCode()){
            200 -> {
                Triple(true , gson.fromJson(resp.body()), null)
            }
            else ->{
                logger.info("Failed to fetch public key")
                Triple(true , null, resp.body())
            }
        }
    }

    fun publishPublicKey(ppk: PrivatePublicKeyPair) : Pair<Boolean, String?>{
        val publicKeyReq = ppk.toPublicKeyAddRequest()

        //val targetUrl = "http://127.0.0.1:16098/pk/${ppk.signingParty}"
        val targetUrl = BASE_URL+"/pk/${ppk.signingParty}"

        val data = gson.toJson(publicKeyReq)
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


        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")


        return if(resp.statusCode() != 200){
            false to "Failed to upload public key: " + resp.body()
        }
        else {
            true to null
        }
    }
}