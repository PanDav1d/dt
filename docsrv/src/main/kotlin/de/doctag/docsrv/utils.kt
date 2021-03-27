package de.doctag.docsrv

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import de.doctag.docsrv.model.FileData
import de.doctag.lib.hexStringToByteArray
import de.doctag.lib.toHex
import de.doctag.lib.toSha1HexString
import kweb.WebBrowser
import kweb.state.KVar
import kweb.state.ReversibleFunction
import org.bson.internal.Base64
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.security.SecureRandom
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Manifest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.streams.asSequence


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


fun getQRCodeImageAsPng(text: String, width: Int, height:Int, margin: Int=10):ByteArrayOutputStream {
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, mapOf(EncodeHintType.MARGIN to margin))
    val pngOutputStream = ByteArrayOutputStream()
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream)
    return pngOutputStream
}

fun getQRCodeImageAsDataUrl(text: String, width: Int, height: Int, margin: Int): String {
    return "data:image/png;base64,"+Base64.encode(getQRCodeImageAsPng(text, width, height, margin).toByteArray())
}

fun BufferedImage.asDataUrlImage() : String{
    val bos = ByteArrayOutputStream()
    ImageIO.write(this, "png", bos)
    return "data:image/png;base64,"+Base64.encode(bos.toByteArray())
}


fun ZonedDateTime.formatDateTime(shortYearFormat: Boolean = false) : String {
    val yearFormat = if(shortYearFormat) "yy" else "yyyy"
    return this.withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ofPattern("dd.MM.${yearFormat} HH:mm"))
}

fun ZonedDateTime.formatDate() = this.withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))


object Resources {
    fun load(relPath: String) = javaClass.classLoader.getResource(relPath).readText()
}

data class DataUrlResult(val contentType:String,val base64Content:String )
fun String.fromDataUrl(): DataUrlResult {
    val (contentType, fileData) = this.removePrefix("data:").split(";base64,")
    return DataUrlResult(contentType, fileData)
}

fun String?.isImage():Boolean {
    return this!= null && ( this.contains("png") || this.contains("gif") || this.contains("jpg"))
}

fun String?.isPdf():Boolean {
    return this!=null && this.contains("pdf")
}

fun Boolean.toYesNoString(): String = if(this) "ja" else "nein"