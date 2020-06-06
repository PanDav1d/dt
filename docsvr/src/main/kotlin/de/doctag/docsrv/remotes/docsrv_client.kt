package de.doctag.docsrv.remotes

import com.fasterxml.jackson.module.kotlin.readValue
import kweb.logger
import de.doctag.docsrv.model.Document
import de.doctag.lib.*
import de.doctag.lib.model.PrivatePublicKeyPair
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object DocServerClient {
    val client: HttpClient by lazy{
        HttpClient.newBuilder().build()
    }

    fun signDocument(doc: Document, ppk: PrivatePublicKeyPair) : Boolean {
        val sigMessage = DoctagSignature.make(loadPrivateKey(ppk.privateKey)!!, loadPublicKey(ppk.publicKey)!!, Duration.ofMinutes(1), ppk.signingParty!!, ppk.owner.firstName + " " + ppk.owner.lastName)
        val rawSigMessage = sigMessage.toDataString

        val request = HttpRequest.newBuilder()
            .uri(URI.create(doc.url!!))
            .timeout(Duration.ofMinutes(1))
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(rawSigMessage, Charsets.UTF_8))
            .build()

        val resp = KeyServerClient.client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")

        return resp.statusCode() == 200
    }

    fun loadDocument(targetUrl : String): Document?{

        val request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMinutes(1))
                .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("fetching doc from url ${targetUrl}")

        return when(resp.statusCode()){
            200 -> {
                try {
                    val doc = getJackson().readValue<Document>(resp.body())
                    doc
                }catch(ex:Exception) {
                    null
                }
            }
            else ->{
                logger.info("Failed to remote document. Statuscode = ${resp.statusCode()}")
                null
            }
        }
    }
}