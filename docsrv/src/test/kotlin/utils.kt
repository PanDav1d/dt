import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.FileData
import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.toSha1HexString
import org.bson.internal.Base64
import java.time.ZonedDateTime

fun makeDocument(content: String) : Pair<Document, List<FileData>> {
    val attachment = FileData("1", "test.txt", Base64.encode(content.toByteArray()))

    val doc = Document(
            "1",
            "https://127.0.0.1:16097/d/1",
            false,
            "test.txt",
            "1",
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