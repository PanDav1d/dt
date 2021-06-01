package de.doctag.docsrv.remotes

import SignatureInputs
import SignatureResult
import com.fasterxml.jackson.module.kotlin.readValue
import de.doctag.docsrv.api.BadRequestException
import de.doctag.docsrv.api.EmbeddedDocument
import de.doctag.docsrv.api.PreparedSignature
import kweb.logger
import de.doctag.docsrv.model.DocumentId
import de.doctag.docsrv.model.EmbeddedSignature
import de.doctag.lib.*
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object AppApiClient {
    val client: HttpClient by lazy{
        HttpClient.newBuilder().build()
    }

    fun checkAuthentication(remote: String, sessionId: String) : Boolean {
        val remoteUrl = remote.removePrefix("http://").removePrefix("https://").removeSuffix("/")
        try {
            val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://${remoteUrl}/app/auth_info".fixHttps()))
                    .header("Accept", "application/json")
                    .header("Cookie", "SESSION=$sessionId")
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

    fun fetchWorkflowToSign(remote:String, sessionId: String, remoteHostname: String, remoteDocumentId: String) : PreparedSignature {


        val request = HttpRequest.newBuilder()
                .uri(URI.create("https://${remote}/app/signature/prepare/${remoteDocumentId}/${remoteHostname}".fixHttps()))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Cookie", "SESSION=$sessionId")
                .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status ${resp.statusCode()}")

        if(resp.statusCode() == 200)
            return getJackson().readValue<PreparedSignature>(resp.body())

        throw BadRequestException("Failed to Prepare Workflow")
    }

    fun uploadWorkflowResultAndTriggerSignature(remote:String, sessionId: String, remoteHostname: String, remoteDocumentId: String, sig: SignatureInputs): SignatureResult?{

        val postUri = "https://${remote}/app/signature/push/${remoteDocumentId}/${remoteHostname}"
        val request = HttpRequest.newBuilder()
                .uri(URI.create(postUri.fixHttps()))
                .header("Accept","application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Cookie", "SESSION=$sessionId")
                .POST(HttpRequest.BodyPublishers.ofString(getJackson().writeValueAsString(sig), Charsets.UTF_8))
                .timeout(Duration.ofMinutes(1))
                .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status code ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")


        return when(resp.statusCode()){
            200 -> {
                try {
                    val doc = getJackson().readValue<SignatureResult>(resp.body())
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