import de.doctag.docsrv.model.EmbeddedSignature
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv.model.Signature
import de.doctag.docsrv.model.WorkflowInputResult
import de.doctag.lib.DoctagSignature
import de.doctag.lib.PublicKeyResponse
import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bson.internal.Base64
import java.time.ZoneId
import java.time.ZonedDateTime

class SerializeDeserializeTest : StringSpec(){
    init{
        "Serialize && Deserialize Embedded Document"{
            val ebs = EmbeddedSignature(
                    listOf(FileData("123", "test.txt", Base64.encode("Test".toByteArray()))),
                    Signature(
                            DoctagSignature(1,1,"abc", "def", "000", "ksfdk", "http:", "", "", "XXX","999"),
                            PublicKeyResponse("a","324", Person("1","f","E", "1","2"), Address("n","n2","s","c","z","c"), "1"),
                            ZonedDateTime.now(ZoneId.of("UTC")),
                            "1",
                            "1",
                            listOf(WorkflowInputResult("1", "1","safd"))
                    )
            )
            val str = ebs.serialize()
            val restoredObj = EmbeddedSignature.load(str)

            restoredObj shouldBe ebs
        }
    }
}