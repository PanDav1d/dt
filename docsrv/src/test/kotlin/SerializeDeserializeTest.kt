import de.doctag.docsrv.model.EmbeddedSignature
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv.model.Signature
import de.doctag.docsrv.model.WorkflowInputResult
import de.doctag.lib.DoctagSignatureData
import de.doctag.lib.PublicKeyResponse
import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import org.bson.internal.Base64
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime




class SerializeDeserializeTest{

    @Test
    fun `Serialize && Deserialize Embedded Document`(){
        val ebs = EmbeddedSignature(
                listOf(FileData("123", "test.txt", Base64.encode("Test".toByteArray()))),
                Signature(
                        DoctagSignatureData("2020-02-13T18:51:00Z","abc", "def", "000", "ksfdk", "http:", "", "", "XXX","999"),
                        PublicKeyResponse("a","324", "01.01.2021", Person("1","f","E", "1","2"), Address("n","n2","s","c","z","c"), "1"),
                        ZonedDateTime.now(ZoneId.of("UTC")),
                        "1",
                        "1",
                        listOf(WorkflowInputResult("1", "1","safd"))
                )
        )
        val str = ebs.serialize()
        val restoredObj = EmbeddedSignature.load(str)

        Assertions.assertEquals(restoredObj, ebs)
    }

}