package de.doctag.docsrv

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import de.doctag.docsrv.model.DocumentId
import kweb.logger
import org.apache.pdfbox.multipdf.Overlay
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import javax.imageio.ImageIO


fun extractDocumentIdOrNull(b64: String): DocumentId? {
    return getImagesFromBase64Content(b64).mapNotNull { img->
        extractQRCode(img)?.let { qrCode ->
            logger.info("Found code ${qrCode}")
            if(DocumentId.isValid(qrCode)){
                DocumentId.parse(qrCode)
            }
            else {
                null
            }
        }
    }.firstOrNull()
}

fun makePdfWithDoctag(url: String, xRel: Float, yRel: Float, relativeWidth: Float) : PDDocument {
    val pdf = PDDocument()
    val firstPage = PDPage(PDRectangle.A4)

    pdf.addPage(firstPage)


    //val firstPage = pdf.getDocumentCatalog().pages.get(0)
    val imgToAdd = PDImageXObject.createFromByteArray(pdf, getQRCodeImageAsPng(url, 300, 300).toByteArray(), "doctag_img.png")

    val contentStream = PDPageContentStream(pdf, firstPage, PDPageContentStream.AppendMode.APPEND, true)

    //Media box returns Points
    val width = firstPage.cropBox.width
    val height = firstPage.cropBox.height

    logger.info("Media box ${width} / ${height}. ${xRel} ${yRel}")
    logger.info("DrawingPosition ${width * xRel} / ${height * yRel}")

    //contentStream.transform(Matrix(0.0f,1.0f,-1.0f,0.0f, width,0.0f ))
    val startX = /*15f **/ width
    val startY = /*12f **/ height

    val boxWidth = firstPage.cropBox.width/relativeWidth

    contentStream.drawImage(imgToAdd, (startX) * xRel - boxWidth / 2, height - (startY) * yRel - boxWidth / 2, boxWidth, boxWidth)
    contentStream.close()

    return pdf
}

fun insertDoctagIntoPDF(b64: String, url: String, xRel: Float, yRel: Float, relativeWidth: Float):String{
    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    val pdf = PDDocument.load(stream)
    pdf.use{
        val watermark = makePdfWithDoctag(url, xRel, yRel, relativeWidth)
        val overlay = Overlay()
        overlay.setInputPDF(pdf)
        overlay.setFirstPageOverlayPDF(watermark)
        overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
        val watermarkedDoc = overlay.overlay(mapOf())

        val output = ByteArrayOutputStream()
        watermarkedDoc.save(output)
        watermarkedDoc.save("test.pdf")
        return Base64.getEncoder().encodeToString(output.toByteArray())
    }
}

fun getImagesFromBase64Content(b64: String) : List<BufferedImage> {
    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    return getImagesFromPdfDocument(stream)
}

fun renderPdfAsImage(b64: String): BufferedImage{
    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    PDDocument.load(stream).use{ pdf ->
        val renderer = PDFRenderer(pdf)
        return renderer.renderImageWithDPI(0, 120.0f)
    }
}

fun extractTextFromPdf(b64: String): String {

    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    val pdf = PDDocument.load(stream)
    pdf.use {
        val pdfStripper = PDFTextStripper()
        return pdfStripper.getText(pdf)
    }
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

        val bi = renderer.renderImageWithDPI(0, 350.0f)
        val outputfile = File("saved.png")
        ImageIO.write(bi, "png", outputfile)

        return allImages.map { it.second.image }.plus(renderer.renderImageWithDPI(0, 350.0f))
    }
}

fun extractQRCode(img: BufferedImage):String?{
    try {
        val binaryBitmap = BinaryBitmap(HybridBinarizer(
                BufferedImageLuminanceSource(img)))
        val qrCodeResult = QRCodeReader().decode(binaryBitmap, mapOf(DecodeHintType.TRY_HARDER to true))
        return qrCodeResult.text
    }
    catch (ex: NotFoundException){
        return null
    }
}