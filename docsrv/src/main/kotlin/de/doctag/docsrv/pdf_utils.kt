package de.doctag.docsrv

import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kweb.logger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO


fun extractDocumentIdOrNull(b64: String, expectedHostName: String): String? {
    return getImagesFromBase64Content(b64).mapNotNull { img->
        extractQRCode(img)?.let { qrCode ->
            logger.info("Found code ${qrCode}")
            if(qrCode.startsWith("http") && qrCode.contains(expectedHostName) && qrCode.contains("/d/")){
                qrCode.split("/d/")[1]
            }
            else {
                null
            }
        }
    }.firstOrNull()
}

fun getImagesFromBase64Content(b64:String) : List<BufferedImage> {
    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    return getImagesFromPdfDocument(stream)
}

fun getImagesFromPdfDocument(input: InputStream) : List<BufferedImage>{
    val pdf = PDDocument.load(input)
    pdf.use { pdf ->
        val allImages = pdf.getDocumentCatalog().pages.flatMap { page ->
            val res = page.resources
            res.xObjectNames.map { it to res.getXObject(it) }
                    .filter { it.second is org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject }
                    .map { it.first to (it.second as org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) }
        }

        val renderer = PDFRenderer(pdf)

        val bi = renderer.renderImageWithDPI(0, 150.0f)
        val outputfile = File("saved.png")
        ImageIO.write(bi, "png", outputfile)

        return allImages.map { it.second.image }.plus(renderer.renderImageWithDPI(0, 300.0f))
    }
}

fun extractQRCode(img: BufferedImage):String?{
    try {
        val binaryBitmap = BinaryBitmap(HybridBinarizer(
                BufferedImageLuminanceSource(img)))
        val qrCodeResult = MultiFormatReader().decode(binaryBitmap)
        return qrCodeResult.text
    }
    catch(ex:NotFoundException){
        return null
    }
}