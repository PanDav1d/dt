package de.docward.docsrv.remotes

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.fromJson
import de.docward.docsrv.DocSignature
import de.docward.docsrv.getJackson
import de.docward.docsrv.keysrv_api.KeyServer
import de.docward.docsrv.loadPrivateKey
import de.docward.docsrv.loadPublicKey
import de.docward.docsrv.model.Document
import de.docward.docsrv.model.PrivatePublicKeyPair
import kweb.gson
import kweb.logger
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object DocServer {
    val client: HttpClient by lazy{
        HttpClient.newBuilder().build()
    }

    fun signDocument(doc: Document, ppk: PrivatePublicKeyPair) : Boolean {
        val sigMessage = DocSignature.make(loadPrivateKey(ppk.privateKey)!!, loadPublicKey(ppk.publicKey)!!, Duration.ofMinutes(1), ppk.signingParty!!, ppk.owner.firstName + " " + ppk.owner.lastName)
        val rawSigMessage = sigMessage.toDataString

        val request = HttpRequest.newBuilder()
            .uri(URI.create(doc.url!!))
            .timeout(Duration.ofMinutes(1))
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(rawSigMessage, Charsets.UTF_8))
            .build()

        val resp = KeyServer.client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Response status ${resp.statusCode()}")
        logger.info("Response string ${resp.body()}")

        return resp.statusCode() == 200
    }

    fun loadDocument(targetUrl : String): Document?{

        val request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofMinutes(1))
                .build()

        val resp = DocServer.client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("fetching doc from url ${targetUrl}")

        return when(resp.statusCode()){
            200 -> {
                try {
                    val doc = getJackson().readValue<Document>(resp.body())
                    logger.info("Successfully fetched remote document = ${resp.body()}")
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