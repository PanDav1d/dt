import de.doctag.docsrv.api.EmbeddedDocument
import de.doctag.docsrv.model.Signature
import de.doctag.docsrv.model.WorkflowInputResult
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SignatureTest : StringSpec() {
    init{
        "Check that the signature is valid"{
            //
            // Given
            //
            val (doc, files) = makeDocument("abc")
            val ppk = makePPK()

            //
            // When
            //
            val currentSig = doc.makeSignature(ppk, "ABC", listOf(WorkflowInputResult("a", "1",null), WorkflowInputResult("b", null, "999")))

            //
            // Then
            //
            currentSig.isValid() shouldBe true
            currentSig.copy(role = "DEF").isValid() shouldBe false
            currentSig.copy(inputs = currentSig.inputs!!.plus(WorkflowInputResult("x","x",null))).isValid() shouldBe false
        }

        "Check that the whole doctag is valid"{
            //
            // Given
            //
            val (doc, files) = makeDocument("abc")
            val ppk = makePPK()

            //
            // When
            //
            val currentSig = doc.makeSignature(ppk, "ABC", listOf(WorkflowInputResult("a", "1",null), WorkflowInputResult("b", null, "999")))
            doc.signatures = (doc.signatures?: listOf()) + currentSig
            val currentSig2 = doc.makeSignature(ppk, "DEF", listOf(WorkflowInputResult("c", "e",null), WorkflowInputResult("x", null, "yyy")))
            doc.signatures = (doc.signatures?: listOf()) + currentSig2

            val edoc = EmbeddedDocument(files, doc)

            //
            // Then
            //
            edoc.validateSignatures() shouldBe true
        }

        "Check that the whole doctag is not valid if signatures are copy pasted"{
            //
            // Given
            //
            val (doc, files) = makeDocument("abc")
            val ppk = makePPK()

            //
            // When
            //
            val currentSig = doc.makeSignature(ppk, "ABC", listOf(WorkflowInputResult("a", "1",null), WorkflowInputResult("b", null, "999")))
            doc.signatures = (doc.signatures?: listOf()) + currentSig + currentSig


            val edoc = EmbeddedDocument(files, doc)

            //
            // Then
            //
            edoc.validateSignatures() shouldBe false
        }
    }
}