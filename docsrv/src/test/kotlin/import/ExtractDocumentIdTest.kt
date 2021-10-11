import de.bwaldvogel.mongo.backend.Assert
import de.doctag.docsrv.extractDocumentIdAndSplitDocument
import de.doctag.docsrv.extractDocumentIds
import org.junit.jupiter.api.Test
import java.util.*

class ExtractDocumentIdTest {
    @Test
    fun `Extract single document id`() {
        // given
        val documentData = Base64.getEncoder().encodeToString(readResourceFileBinary("import/extract_document_id_test/single_qr.pdf"))

        //when
        val extractionResult = extractDocumentIds(documentData)

        //Then
        extractionResult.single().let{
            Assert.equals(it.documentId.fullUrl, "https://adam.kd.doctag.de/d/1.0384.2892.459001531")
            Assert.equals(it.pageIdx, 1)
        }
    }

    @Test
    fun `Extract multiple document ids`() {
        // given
        val documentData = Base64.getEncoder().encodeToString(readResourceFileBinary("import/extract_document_id_test/multi_qr.pdf"))

        //when
        val extractionResult = extractDocumentIds(documentData).toList()

        //Then
        extractionResult.get(0).let {
            Assert.equals(it.documentId.fullUrl, "https://adam.kd.doctag.de/d/4.0162852095701101")
            Assert.equals(it.pageIdx, 1)
        }

        extractionResult.get(1).let {
            Assert.equals(it.documentId.fullUrl, "https://adam.kd.doctag.de/d/4.0162852095701103.410131.922570972")
            Assert.equals(it.pageIdx, 2)
        }
    }

    @Test
    fun `Split multi page pdf`(){
        // given
        val documentData = Base64.getEncoder().encodeToString(readResourceFileBinary("import/extract_document_id_test/multi_qr.pdf"))

        // when
        val parts = extractDocumentIdAndSplitDocument(documentData).toList()

        // then
        Assert.equals(parts.size, 2)
        val p1 = parts[0]
        val p2 = parts[1]

        Assert.equals(extractDocumentIds(p1.b64).single().documentId.fullUrl,"https://adam.kd.doctag.de/d/4.0162852095701101")
        Assert.equals(extractDocumentIds(p2.b64).single().documentId.fullUrl,"https://adam.kd.doctag.de/d/4.0162852095701103.410131.922570972")

    }

    @Test
    fun `Triple page pdf`(){
        // given
        val documentData = Base64.getEncoder().encodeToString(readResourceFileBinary("import/extract_document_id_test/triple_qr.pdf"))

        // when
        val parts = extractDocumentIdAndSplitDocument(documentData).toList()

        // then
        Assert.equals(parts.size, 3)
        val p1 = parts[0]
        val p2 = parts[1]
        val p3 = parts[2]

        Assert.equals(extractDocumentIds(p1.b64).single().documentId.fullUrl,"https://adam.kd.doctag.de/d/06.1695291010161")
        Assert.equals(extractDocumentIds(p2.b64).single().documentId.fullUrl,"https://adam.kd.doctag.de/d/06.16952910101601.95228821201412")
        Assert.equals(extractDocumentIds(p3.b64).single().documentId.fullUrl,"https://adam.kd.doctag.de/d/06.16952910101601.952288212014104.9516453940923")
    }
}