import de.doctag.lib.*
import de.doctag.lib.model.PublicKeyVerification
import de.doctag.lib.model.PublicKeyVerificationResult
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object DoctagKeyClient{
    val client: HttpClient by lazy{
        HttpClient.newBuilder().build()
    }

    fun verifiyDoctagInstanceHasPrivateKey(instanceUrl : String, publicKey: String) : Boolean {

        val seed = generateRandomString(128)
        val pk = loadPublicKey(publicKey)!!
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://${instanceUrl}/k/${publicKeyFingerprint(pk)}/verify/$seed".fixHttps()))
            .timeout(Duration.ofMinutes(1))
            .build()

        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())

        logger.info("Checking if doctag instance owns private key for public key $publicKey")

        return when(resp.statusCode()){
            200 -> {
                val obj = getJackson().readValue(resp.body(), PublicKeyVerificationResult::class.java)
                verifySignature(pk, obj.message, obj.signature)
            }
            else ->{
                logger.info("Failed to aquire verification")
                false
            }
        }
    }

    fun pushVerificationToDoctagInstance(instanceUrl: String, publicKey: String, verification: PublicKeyVerification):Boolean{
        val pk = loadPublicKey(publicKey)!!
        val data = getJackson().writeValueAsString(verification)
        val req = HttpRequest.newBuilder()
            .uri(URI.create("https://$instanceUrl/k/${publicKeyFingerprint(pk)}/verification".fixHttps()))
            .header("Content-Type", "application/json; charset=utf-8")
            .timeout(Duration.ofMinutes(1))
            .PUT(HttpRequest.BodyPublishers.ofString(data, Charsets.UTF_8))
            .build()

        val resp = client.send(req, HttpResponse.BodyHandlers.ofString())

        return when(resp.statusCode()){
            200 -> {
                true
            }
            else ->{
                logger.info("Failed to push verification to client. Reason: ${resp.body()}")
                false
            }
        }
    }
}

