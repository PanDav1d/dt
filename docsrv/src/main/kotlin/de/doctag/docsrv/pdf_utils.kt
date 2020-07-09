package de.doctag.docsrv

import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.apache.pdfbox.pdmodel.PDDocument
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*


fun getImagesFromBase64Content(b64:String) : List<BufferedImage> {
    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    return getImagesFromPdfDocument(stream)
}

fun getImagesFromPdfDocument(input: InputStream) : List<BufferedImage>{
    val pdf = PDDocument.load(input)
    val allImages = pdf.getDocumentCatalog().pages.flatMap { page->
        val res = page.resources
        res.xObjectNames.map { it to  res.getXObject(it)}
                .filter { it.second is org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject }
                .map { it.first to (it.second as org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject)}
    }

    return allImages.map { it.second.image }
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