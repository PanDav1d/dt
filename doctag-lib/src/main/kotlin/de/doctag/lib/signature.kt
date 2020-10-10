package de.doctag.lib

import com.fasterxml.jackson.annotation.JsonIgnore
import de.doctag.lib.model.PrivatePublicKeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

data class SignatureLoadingResult(
        val valid: Boolean,
        val rawSignedMessage: String,
        val signedMessage: DoctagSignature?,
        val publicKey: PublicKeyResponse?,
        val message: String?
)

data class DoctagSignature(
        val validFrom: Long,
        val validTill: Long,
        val randomBuffer: String,
        val keyFingerprint: String,
        val signingParty: String,
        val signingUser: String,
        val documentUrl: String?,
        val signature: String
)
{
    fun toDataString(): String
        = "${signature?:""};$validFrom;$validTill;$randomBuffer;$keyFingerprint;${signingParty};${signingUser.replace(';',',')};${documentUrl?:""}".trimEnd(';')

    val validFromDateTime : ZonedDateTime
        get() {
            val i = Instant.ofEpochSecond(validFrom)
            return ZonedDateTime.ofInstant(i, ZoneId.of("UTC"))
        }

    companion object{

        fun make(priv: String, pub:String, validity: Duration, signingParty: String, signingUser: String) = make(loadPrivateKey(priv)!!, loadPublicKey(pub)!!, validity, signingParty, signingUser)

        fun make(priv: PrivateKey, pub: PublicKey, validity: Duration, signingParty: String, signingUser: String) = makeWithUrl(priv, pub, validity, signingParty, signingUser, null)

        fun makeWithUrl(priv: PrivateKey, pub: PublicKey, validity: Duration, signingParty: String, signingUser: String, url:String?): DoctagSignature{
            val randomBytes = ByteArray(12)
            SecureRandom().nextBytes(randomBytes)
            val validFrom = ZonedDateTime.now().withSecond(0).withZoneSameInstant(ZoneId.of("UTC"))
            val validOn = ZonedDateTime.now().withSecond(0).plus(validity).withZoneSameInstant(ZoneId.of("UTC"))

            val fingerprint = publicKeyFingerprint(pub)

            val rawSig = DoctagSignature(validFrom.toEpochSecond(), validOn.toEpochSecond(), Base64.getEncoder().encodeToString(randomBytes), fingerprint, signingParty, signingUser, url,"")
            val rawSigString = rawSig.toDataString()

            val sig = makeSignature(priv, rawSigString)

            return rawSig.copy(signature = sig)
        }

        fun makeWithPPK(ppk: PrivatePublicKeyPair, validity: Duration, url:String?) : DoctagSignature{
            val user = "${ppk.owner?.firstName} ${ppk.owner.lastName}"
            return makeWithUrl(loadPrivateKey(ppk.privateKey)!!, loadPublicKey(ppk.publicKey)!!,validity, ppk.signingParty!!, user, url)
        }

        fun fromCsv(tokens: List<String>):DoctagSignature {
            val signature = tokens[0]
            val validFrom = tokens[1]
            val validTill = tokens[2]
            val randomBuffer = tokens[3]
            val keyFingerprint = tokens[4]
            val signingParty = tokens[5]
            val signingUser = tokens[6]
            val url = tokens.getOrNull(7)

            return DoctagSignature(validFrom.toLong(), validTill.toLong(), randomBuffer, keyFingerprint, signingParty,signingUser, url, signature)
        }

        fun load(msg: String): SignatureLoadingResult{
            if(!msg.contains(";"))
                return SignatureLoadingResult(false, msg,null, null, "Format does not match")

            val tokens = msg.split(";")

            val signatureObj = fromCsv(tokens)
            val (successfullyLoaded, sig, error) = KeyServerClient.loadPublicKey(signatureObj.signingParty, signatureObj.keyFingerprint)

            return if(successfullyLoaded) {
                val isValid = verifySignature(loadPublicKey(sig!!.publicKey)!!, ";"+msg.substringAfter(";"), signatureObj.signature)
                if(isValid){
                    SignatureLoadingResult(true, msg, signatureObj, sig,"ok")
                } else {
                    SignatureLoadingResult(false, msg, null, null,"Signature verification failed.")
                }
            } else {
                SignatureLoadingResult(false, msg, null, null, error)
            }
        }
    }
}