import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.FileData
import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.toSha1HexString
import org.bson.internal.Base64
import java.time.ZonedDateTime

fun makeDocument(content: String, hostname: String = "127.0.0.1:16097", docId : String = "1" ) : Pair<Document, List<FileData>> {
    val attachment = FileData("", "test.txt", Base64.encode(content.toByteArray()))
    attachment._id = attachment.base64Content!!.toSha1HexString()

    val doc = Document(
            docId,
            "https://$hostname/d/1",
            false,
            "test.txt",
            attachment._id,
            attachment.base64Content?.toSha1HexString(),
            listOf(),
            ZonedDateTime.now(),
            listOf(),
            null
    )

    return doc to listOf(attachment)
}

fun makePPK():PrivatePublicKeyPair{
    return PrivatePublicKeyPair.make("test", "127.0.0.1", Address("","","","","",""), Person("", "","",""))
}