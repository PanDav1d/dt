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

    fun checkHealth(remote: String) : Boolean {
        val remoteUrl = remote.removePrefix("http://").removePrefix("https://").removeSuffix("/")


        try {
            val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://${remoteUrl}/health"))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofMinutes(1))
                    .build()
            logger.info("Sending request to ${request.uri()}")
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.statusCode() == 200
        }
        catch(ex: Exception){
            logger.info("Health check failed: ${ex.message}")
            return false
        }
    }

    fun signDocument(doc: Document, ppk: PrivatePublicKeyPair) : Boolean {
        val sigMessage = DoctagSignature.makeWithUrl(loadPrivateKey(ppk.privateKey)!!, loadPublicKey(ppk.publicKey)!!, Duration.ofMinutes(1), ppk.signingParty!!, ppk.owner.firstName + " " + ppk.owner.lastName, doc.url)
        val rawSigMessage = sigMessage.toDataString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(doc.url!!))
            .timeout(Duration.ofMinutes(1))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(rawSigMessage, Charsets.UTF_8))
            .build()

        logger.info("Destination: ${doc.url}")
        logger.info("data ${rawSigMessage}")

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")

        return resp.statusCode() == 200
    }

    fun loadDocument(targetUrl : String): Document?{

        val request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept","application/json")
                .timeout(Duration.ofMinutes(1))
                .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("fetching doc from url ${targetUrl}")
        logger.info("Response status code ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")


        return when(resp.statusCode()){
            200 -> {
                try {
                    val doc = getJackson().readValue<Document>(resp.body())
                    doc
                }catch(ex:Exception) {
                    logger.error("Failed to parse json")
                    logger.error(ex.message)
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