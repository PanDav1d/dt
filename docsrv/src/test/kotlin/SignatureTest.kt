import de.doctag.docsrv.api.EmbeddedDocument
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv.model.Signature
import de.doctag.docsrv.model.WorkflowInputResult
import de.doctag.lib.toSha1HexString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SignatureTest  {

    @Test
    fun `Check that the signature is valid`() {
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
        Assertions.assertEquals(currentSig.isValid(), true)
        Assertions.assertEquals(currentSig.copy(role = "DEF").isValid(), false)
        Assertions.assertEquals(currentSig.copy(inputs = currentSig.inputs!!.plus(WorkflowInputResult("x","x",null))).isValid(), false)
        Assertions.assertEquals(currentSig.copy(signed = currentSig.signed!!.plusMinutes(2)).isValid(), false)
        Assertions.assertEquals(currentSig.copy(signed = currentSig.signed!!.minusMinutes(2)).isValid(), false)
    }

    @Test
    fun `Check that the whole doctag is valid`(){
        //
        // Given
        //
        val (doc, files) = makeDocument("abc")
        val ppk = makePPK()

        //
        // When
        //
        val currentSig = doc.makeSignature(ppk, "ABC", listOf(WorkflowInputResult("a", "1",null), WorkflowInputResult("b", null, null)))
        doc.signatures = (doc.signatures?: listOf()) + currentSig
        val currentSig2 = doc.makeSignature(ppk, "DEF", listOf(WorkflowInputResult("c", "e",null), WorkflowInputResult("x", null, null)))
        doc.signatures = (doc.signatures?: listOf()) + currentSig2

        val edoc = EmbeddedDocument(files, doc)

        //
        // Then
        //
        Assertions.assertEquals(edoc.validateSignatures(), true)
    }

    @Test
    fun `Check that the whole doctag is valid including file hash of workflow input`(){
        //
        // Given
        //
        val (doc, files) = makeDocument("abc")
        val workflowFileInput = FileData("abc".toSha1HexString(), "test.jpeg", "abc")

        val ppk = makePPK()

        //
        // When
        //
        val currentSig = doc.makeSignature(ppk, "ABC", listOf(WorkflowInputResult("a", "1",null), WorkflowInputResult("b", null, workflowFileInput._id)))
        doc.signatures = (doc.signatures?: listOf()) + currentSig
        val currentSig2 = doc.makeSignature(ppk, "DEF", listOf(WorkflowInputResult("c", "e",null), WorkflowInputResult("x", null, null)))
        doc.signatures = (doc.signatures?: listOf()) + currentSig2

        val edoc = EmbeddedDocument(files+workflowFileInput, doc)
        val edocInvalid = EmbeddedDocument(files, doc)
        //
        // Then
        //

        Assertions.assertEquals(edoc.validateSignatures(), true)
        Assertions.assertEquals(edocInvalid.validateSignatures(), false)
    }

    @Test
    fun `Check that the whole doctag is not valid if signatures are copy pasted`(){
        //
        // Given
        //
        val (doc, files) = makeDocument("abc")
        val ppk = makePPK()

        //
        // When
        //
        val currentSig = doc.makeSignature(ppk, "ABC", listOf(WorkflowInputResult("a", "1",null), WorkflowInputResult("b", "999", null)))
        doc.signatures = (doc.signatures?: listOf()) + currentSig + currentSig


        val edoc = EmbeddedDocument(files, doc)

        //
        // Then
        //
        Assertions.assertEquals(edoc.validateSignatures(), false)
    }

}