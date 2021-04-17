package de.doctag.lib

import de.doctag.lib.model.PrivatePublicKeyPair
import java.lang.IllegalStateException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


data class SignatureLoadingResult(
    val valid: Boolean,
    val rawSignedMessage: String,
    val signedMessage: DoctagSignatureData?,
    val publicKey: PublicKeyResponse?,
    val message: String?
)


data class DoctagSignatureData(
    val validFrom: String,
    val randomBuffer: String,
    val keyFingerprint: String,
    val signingDoctagInstance: String?,
    val signingUser: String,
    val documentUrl: String?,
    val documentHash: String?,
    val workflowHash: String?,
    val previousSignaturesHash: String?,
    val signature: String
)
{
    fun toDataString(): String
        = "$signature?;$validFrom;$randomBuffer;$keyFingerprint;${signingDoctagInstance};${signingUser.replace(';', ',')};${documentUrl?:""};${documentHash?:""};${workflowHash?:""};${previousSignaturesHash?:""}".trimEnd(';')

    val validFromDateTime : ZonedDateTime
        get() {
            val ta = DateTimeFormatter.ISO_DATE_TIME.parse(validFrom)
            val i = Instant.from(ta)
            return ZonedDateTime.ofInstant(i, ZoneId.of("UTC"))
        }

    companion object{

        fun make(priv: String, pub: String, validity: Duration, signingDoctagInstance: String, signingUser: String) = make(loadPrivateKey(priv)!!, loadPublicKey(pub)!!, validity, signingDoctagInstance, signingUser)

        fun make(priv: PrivateKey, pub: PublicKey, validity: Duration, signingDoctagInstance: String, signingUser: String) = makeWithUrl(priv, pub, validity, signingDoctagInstance, signingUser, null, null, null, null)

        fun makeWithUrl(priv: PrivateKey, pub: PublicKey, validity: Duration, signingDoctagInstance: String, signingUser: String, url: String?, documentHash: String?, workflowHash: String?, previousSignatures: String?): DoctagSignatureData{
            val randomBytes = ByteArray(12)
            SecureRandom().nextBytes(randomBytes)
            val validFrom = ZonedDateTime.now().withSecond(0).withZoneSameInstant(ZoneId.of("UTC"))

            val fingerprint = publicKeyFingerprint(pub)

            val rawSig = DoctagSignatureData(DateTimeFormatter.ISO_DATE_TIME.format(validFrom), Base64.getEncoder().encodeToString(randomBytes), fingerprint, signingDoctagInstance, signingUser, url, documentHash, workflowHash, previousSignatures, "")
            val rawSigString = rawSig.toDataString()

            logger.info("RawSigMessage: $rawSigString")

            val sig = makeSignature(priv, rawSigString)

            return rawSig.copy(signature = sig)
        }

        fun makeWithPPK(ppk: PrivatePublicKeyPair, validity: Duration, url: String?, documentHash: String?, workflowHash: String?, previousSignatureHash: String?) : DoctagSignatureData{
            val user = "${ppk.owner.firstName} ${ppk.owner.lastName}"

            val privKey = loadPrivateKey(ppk.privateKey) ?: throw IllegalStateException("Unable to load private key")
            val pubKey = loadPublicKey(ppk.publicKey) ?: throw IllegalStateException("Unable to load public key")
            val instanceUrl = ppk.signingDoctagInstance ?: throw IllegalStateException("Unable to detect signingInstanceUrl")

            return makeWithUrl(privKey, pubKey, validity, instanceUrl, user, url, documentHash, workflowHash, previousSignatureHash)
        }

        fun fromCsv(tokens: List<String>):DoctagSignatureData {
            val signature = tokens[0]
            val validFrom = tokens[1]
            val randomBuffer = tokens[2]
            val keyFingerprint = tokens[3]
            val signingDoctagInstance = tokens[4]
            val signingUser = tokens[5]
            val url = tokens.getOrNull(6)
            val documentHash = tokens.getOrNull(7)
            val workflowHash: String? = tokens.getOrNull(8)
            val prevSigHash = tokens.getOrNull(9)

            return DoctagSignatureData(validFrom, randomBuffer, keyFingerprint, signingDoctagInstance, signingUser, url, documentHash, workflowHash, prevSigHash, signature)
        }

        fun load(msg: String): SignatureLoadingResult{
            if(!msg.contains(";"))
                return SignatureLoadingResult(false, msg, null, null, "Format does not match")

            val tokens = msg.split(";")

            val signatureObj = fromCsv(tokens)
            val (successfullyLoaded, sig, error) = KeyServerClient.loadPublicKey(signatureObj.signingDoctagInstance!!, signatureObj.keyFingerprint)

            return if(successfullyLoaded) {
                val isValid = verifySignature(loadPublicKey(sig!!.publicKey)!!, ";" + msg.substringAfter(";"), signatureObj.signature)
                if(isValid){
                    SignatureLoadingResult(true, msg, signatureObj, sig, "ok")
                } else {
                    SignatureLoadingResult(false, msg, null, null, "Signature verification failed.")
                }
            } else {
                SignatureLoadingResult(false, msg, null, null, error)
            }
        }
    }
}