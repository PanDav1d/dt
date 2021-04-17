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
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import kotlin.random.Random
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature




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
    val imgToAdd = PDImageXObject.createFromByteArray(pdf, getQRCodeImageAsPng(url, 100, 100, margin = 5).toByteArray(), "doctag_img.png")

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

    contentStream.drawImage(imgToAdd, (startX) * xRel - boxWidth / 2 + 0.1f*boxWidth, height - (startY) * yRel - boxWidth / 2+ 0.1f*boxWidth, 0.8f*boxWidth, 0.8f*boxWidth)
    contentStream.close()

    return pdf
}

fun PDDocument.enableProtection(){
    val ap = AccessPermission()
    ap.setCanModify(false)
    ap.setCanFillInForm(false)
    ap.setCanModifyAnnotations(false)
    ap.setReadOnly()
    val spp = StandardProtectionPolicy(Random.nextLong().toString(), "", ap)
    spp.encryptionKeyLength = 128
    this.protect(spp)
}

fun signDetached(document: PDDocument) {

    // create signature dictionary
    val signature = PDSignature()
    signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
    signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED)
    signature.name = "Example User"
    signature.location = "Los Angeles, CA"
    signature.reason = "Testing"
    // TODO extract the above details from the signing certificate? Reason as a parameter?

    // the signing date, needed for valid signature
    signature.signDate = Calendar.getInstance()

    // Optional: certify

    val signatureOptions = SignatureOptions()
    // Size can vary, but should be enough for purpose.
    signatureOptions.preferredSignatureSize = SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2
    // register signature dictionary and sign interface
    document.addSignature(signature, signatureOptions)


}

fun insertDoctagIntoPDF(b64: String, url: String, xRel: Float, yRel: Float, relativeWidth: Float, formData: Map<String, String>? = null):String{
    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    val pdf = PDDocument.load(stream)

    pdf.use{
        formData?.forEach { fieldName, value ->
            pdf.setField(fieldName, value)
        }

        val watermark = makePdfWithDoctag(url, xRel, yRel, relativeWidth)
        val overlay = Overlay()
        overlay.setInputPDF(pdf)
        overlay.setFirstPageOverlayPDF(watermark)
        overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
        val watermarkedDoc = overlay.overlay(mapOf())

        val output = ByteArrayOutputStream()

        watermarkedDoc.enableProtection()
        watermarkedDoc.save(output)
        watermarkedDoc.save("test.pdf")
        return Base64.getEncoder().encodeToString(output.toByteArray())
    }
}



fun PDDocument.setField(name: String, value: String?) {
    val docCatalog: PDDocumentCatalog = this.documentCatalog
    val acroForm = docCatalog.acroForm
    val field = acroForm.getField(name)
    if (field != null) {
        field.setValue(value)
    } else {
        System.err.println("No field found with name:$name")
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

fun extractFormFieldsFromPdf(b64: String) : List<PDField> {
    val stream = ByteArrayInputStream(Base64.getDecoder().decode(b64))
    val pdf = PDDocument.load(stream)

    val docCatalog: PDDocumentCatalog = pdf.documentCatalog
    val acroForm = docCatalog.acroForm
    return acroForm?.fields?.flatMap { extractFieldList(it).toList() } ?: listOf()
}

private fun extractFieldList(field: PDField) : List<PDField> {

    val result = mutableListOf<PDField>()

    if (field is PDNonTerminalField) {
        for (child in field.children) {
            val subfields : List<PDField> = extractFieldList(child).toList()
            result.addAll(subfields)
        }
    }
    else {
        result.add(field)
    }
    return result
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