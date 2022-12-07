import de.doctag.docsrv.trimImage
import de.doctag.lib.logger
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO


fun mergeSignatureWithBackgroundImage(background: String?, signature: String) : String{
    if(background.isNullOrBlank())
        return signature

    val strippedSig = if(signature.contains(","))
        signature.substringAfter(",")
    else
        signature

    val strippedBackground = if(background.contains(","))
        background.substringAfter(",")
    else
        background

    val data = Base64.getDecoder().decode(strippedSig)
    var trimmedSignature = trimImage(ImageIO.read(ByteArrayInputStream(data)))

    val stampData = Base64.getDecoder().decode(strippedBackground)
    val stamp = ImageIO.read(ByteArrayInputStream(stampData))

    val rW = trimmedSignature.width.toDouble()/stamp.width
    logger.info("Width Ratio is ${rW}")
    if(rW>1){
        trimmedSignature = scale1(trimmedSignature, 1.0/rW)
    }
    val rH = trimmedSignature.height.toDouble()/stamp.height
    logger.info("Height Ratio is ${rH}")
    if(rH>1){
        trimmedSignature = scale1(trimmedSignature, 1.0/rH)
    }


    val g2d = stamp.createGraphics()
    g2d.drawImage(trimmedSignature, stamp.width/2-trimmedSignature.width/2, stamp.height/2-trimmedSignature.height/2, null)

    val bos = ByteArrayOutputStream()
    ImageIO.write(stamp, "PNG", bos)
    return Base64.getEncoder().encodeToString(bos.toByteArray())
}

private fun scale1(before: BufferedImage, scale: Double): BufferedImage {
    val w = before.width
    val h = before.height
    // Create a new image of the proper size
    val w2 = (w * scale).toInt()
    val h2 = (h * scale).toInt()
    val after = BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB)
    val scaleInstance: AffineTransform = AffineTransform.getScaleInstance(scale, scale)
    val scaleOp = AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR)
    scaleOp.filter(before, after)
    return after
}
