package de.doctag.keysrv

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import kweb.WebBrowser
import kweb.gson
import kweb.logger
import kweb.state.KVar
import kweb.state.ReversibleFunction
import org.bson.internal.Base64
import java.lang.reflect.Type
import java.math.BigInteger
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions


inline fun <O, reified T : Any?> KVar<T>.propertyOrDefault(property: KProperty1<T, O?>, default: O): KVar<O> {
    return this.map(object : ReversibleFunction<T, O>("prop: ${property.name}") {

        private val kClass = T::class
        private val copyFunc = kClass.memberFunctions.firstOrNull { it.name == "copy" }
            ?: error("Can't find `copy` function in class ${kClass.simpleName}, are you sure it's a data object?")
        private val instanceParam = copyFunc.instanceParameter
            ?: error("Unable to obtain instanceParam")
        private val fieldParam = copyFunc.parameters.firstOrNull { it.name == property.name }
            ?: error("Unable to identify parameter for ${property.name} in ${kClass.simpleName}.copy() function")

        override fun invoke(from: T): O = property.invoke(from) ?: default

        override fun reverse(original: T, change: O): T = copyFunc.callBy(mapOf(instanceParam to original, fieldParam to change)) as T
    })
}

val WebBrowser.urlParameters:Map<String,String>
    get() = this.url.value.substringAfter('?').split("&").map { tokens -> tokens.split("=") }.map { it[0] to it[1] }.toMap()

fun checkPasswordHash(currentPasswordHash:String?, password:String):Boolean{
    currentPasswordHash ?: return false

    val (format, iterations, salt, hash) = currentPasswordHash.split(":")

    val actualHash = generatePasswordHash(password, salt, iterations.toInt())

    return actualHash == currentPasswordHash
}

fun generatePasswordHash(password: String, initialSalt:String? = null, iterations: Int = 1000): String? {
    val chars = password.toCharArray()
    val salt = initialSalt?.hexStringToByteArray() ?: getSalt()
    val spec = PBEKeySpec(chars, salt, iterations, 64 * 8)
    val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = skf.generateSecret(spec).encoded
    return "pbkdf2:"+iterations.toString() + ":" + toHex(salt) + ":" + toHex(hash)
}

fun getSalt(): ByteArray {
    val sr: SecureRandom = SecureRandom.getInstance("SHA1PRNG")
    val salt = ByteArray(16)
    sr.nextBytes(salt)
    return salt
}

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







internal object GsonHelper {
    val ZDT_DESERIALIZER: JsonDeserializer<ZonedDateTime?> = object : JsonDeserializer<ZonedDateTime?> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): ZonedDateTime {
            val jsonPrimitive: JsonPrimitive = json.getAsJsonPrimitive()
            try {

                // if provided as String - '2011-12-03T10:15:30+01:00[Europe/Paris]'
                if (jsonPrimitive.isString()) {
                    return ZonedDateTime.parse(jsonPrimitive.getAsString(), DateTimeFormatter.ISO_ZONED_DATE_TIME)
                }

                // if provided as Long
                if (jsonPrimitive.isNumber()) {
                    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(jsonPrimitive.getAsLong()), ZoneId.systemDefault())
                }
            } catch (e: RuntimeException) {
                throw JsonParseException("Unable to parse ZonedDateTime", e)
            }
            throw JsonParseException("Unable to parse ZonedDateTime")
        }
    }
}
