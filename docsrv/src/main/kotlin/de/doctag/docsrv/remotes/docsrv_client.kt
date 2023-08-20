package de.doctag.docsrv.remotes

import com.fasterxml.jackson.module.kotlin.readValue
import de.doctag.docsrv.api.EmbeddedDocument
import de.doctag.docsrv.api.NotifyRequest
import kweb.logger
import de.doctag.docsrv.model.DocumentId
import de.doctag.docsrv.model.EmbeddedSignature
import de.doctag.lib.*
import de.doctag.docsrv.sanitizeUrl
import kweb.util.toJson
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

    /**
     * Überprüft den Gesundheitsstatus eines Remote-Servers durch den Aufruf eines 'health'-Endpunkts.
     *
     * @param remote Die URL des Remote-Servers.
     * @return Ein Boolean-Wert, der angibt, ob der Remote-Server erfolgreich erreicht wurde.
     */
    fun checkHealth(remote: String) : Boolean {
        val remoteUrl = sanitizeUrl(remote)
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://${remoteUrl}/health".fixHttps()))
                .header("Accept", "application/json")
                .timeout(Duration.ofMinutes(1))
                .build()
            logger.info("Sending request to ${request.uri()}")
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.statusCode() == 200
        } catch(ex: Exception){
            logger.info("Health check failed: ${ex.message}")
            false
        }
    }

    fun pushSignature(doctagUrl: String, sig:EmbeddedSignature) : Boolean {

        val rawSigMessage = sig.serialize()

        val pushUrl = DocumentId.parse(doctagUrl)

        val request = HttpRequest.newBuilder()
                .uri(URI.create("https://${pushUrl.hostname}/d/${pushUrl.id}/${pushUrl.hostname}".fixHttps()))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(rawSigMessage, Charsets.UTF_8))
                .build()

        logger.info("Destination: $doctagUrl")
        logger.info("data $rawSigMessage")

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")

        return resp.statusCode() == 200
    }

    fun notifyDocumentDidChange(instanceUrl: String, changedDocumentUrl: String) : Boolean {


        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://${instanceUrl}/d/notifyChanges/".fixHttps()))
            .timeout(Duration.ofMinutes(1))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(NotifyRequest(changedDocumentUrl).toJson(), Charsets.UTF_8))
            .build()

        logger.info("Signaling change of document: $changedDocumentUrl to instance $instanceUrl")

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")

        return resp.statusCode() == 200
    }

    fun loadDocument(targetUrl : String): EmbeddedDocument?{

        val request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl.fixHttps()))
                .header("Accept","application/json")
                .timeout(Duration.ofMinutes(1))
                .build()

        logger.info("fetching doc from url ${targetUrl}")

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())


        logger.info("Response status code ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")


        return when(resp.statusCode()){
            200 -> {
                try {
                    val doc = getJackson().readValue<EmbeddedDocument>(resp.body())
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