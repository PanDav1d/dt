package de.doctag.lib

import java.math.BigInteger
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

fun String.hexStringToByteArray(): ByteArray {
    val len = this.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] = ((Character.digit(this[i], 16) shl 4)
                + Character.digit(this[i + 1], 16)).toByte()
        i += 2
    }
    return data
}

fun toHex(array: ByteArray): String {
    val bi = BigInteger(1, array)
    val hex = bi.toString(16)
    val paddingLength = array.size * 2 - hex.length
    return if (paddingLength > 0) {
        String.format("%0" + paddingLength + "d", 0) + hex
    } else {
        hex
    }
}

fun loadPrivateKey(key64: String?): PrivateKey? {
    val clear: ByteArray = Base64.getDecoder().decode(key64)
    val keySpec = PKCS8EncodedKeySpec(clear)
    val fact = KeyFactory.getInstance("EC")
    val priv = fact.generatePrivate(keySpec)
    Arrays.fill(clear, 0.toByte())
    return priv
}

fun loadPublicKey(stored: String?): PublicKey? {
    val data: ByteArray = Base64.getDecoder().decode(stored)
    val spec = X509EncodedKeySpec(data)
    val fact = KeyFactory.getInstance("EC")
    return fact.generatePublic(spec)
}


fun publicKeyFingerprintBinary(pub: PublicKey): ByteArray{
    val fact = KeyFactory.getInstance("EC")
    val spec = fact.getKeySpec(
            pub,
            X509EncodedKeySpec::class.java
    )
    return sha1(spec.encoded).copyOfRange(0,4)
}

fun publicKeyFingerprint(pub: PublicKey): String{
    return toHex(publicKeyFingerprintBinary(pub))
}

fun savePrivateKey(priv: PrivateKey?): String? {
    val fact = KeyFactory.getInstance("EC")
    val spec = fact.getKeySpec(
            priv,
            PKCS8EncodedKeySpec::class.java
    )
    val packed = spec.encoded
    val key64: String = Base64.getEncoder().encodeToString(packed)
    Arrays.fill(packed, 0.toByte())
    return key64
}


fun savePublicKey(publ: PublicKey?): String? {
    val fact = KeyFactory.getInstance("EC")
    val spec = fact.getKeySpec(
            publ,
            X509EncodedKeySpec::class.java
    )
    return Base64.getEncoder().encodeToString(spec.encoded)
}

fun makeSignature(privK: PrivateKey, msg: String) : String {
    val ecdsaSign: Signature = Signature.getInstance("SHA256withECDSA")
    ecdsaSign.initSign(privK)

    ecdsaSign.update(msg.toByteArray())
    val signature: ByteArray = ecdsaSign.sign()
    val sig: String = Base64.getEncoder().encodeToString(signature)

    return sig
}

fun verifySignature(pk : PublicKey, message: String, base64Signature: String) : Boolean{
    val ecdsaVerify = Signature.getInstance("SHA256withECDSA")
    ecdsaVerify.initVerify(pk)
    ecdsaVerify.update(message.toByteArray(Charsets.UTF_8))
    return ecdsaVerify.verify(Base64.getDecoder().decode(base64Signature))
}

fun sha1(array: ByteArray): ByteArray {
    val md = MessageDigest.getInstance("SHA1")
    return md.digest(array)
}

fun String.toSha1HexString():String{
    return toHex(sha1(toByteArray()))
}