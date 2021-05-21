package api

import WithTestDatabase
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import de.doctag.docsrv.Config
import de.doctag.docsrv.DocSrvConfig
import de.doctag.docsrv.kwebFeature
import de.doctag.docsrv.model.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import makeDocument
import makePPK
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import setupApi
import java.io.File
import java.net.InetSocketAddress

class TestConfig(override val dbConnection: String, override val dbName: String) : DocSrvConfig

const val DB_NAME = "docserver"

class PostSignatureTests : WithTestDatabase() {


    @Test
    fun `Fetch Document Test`() {

        //
        // Given
        //
        val doc = makeDocument("aaabbbccc")
        dbContext.documents.save(doc.first)
        dbContext.files.insertMany(doc.second)

        val ppk = makePPK()
        val sig = doc.first.makeSignature(ppk, null, null)

        val embeddedSignature = EmbeddedSignature(listOf(), sig)

        val parsedUrl = DocumentId.parse(doc.first.url!!)

        //
        // When
        //

        val signatureFile = File.createTempFile("12323", "9999")
        signatureFile.writeText(embeddedSignature.serialize())

        val api = setupApi()

        val response = api.fetchDoctagDocumentWithHttpInfo(
            parsedUrl.id
        )

        //
        // Then
        //
        Assertions.assertEquals(HttpStatusCode.OK, response.statusCode)
        val currentDocument = dbContext.documents.findOne(Document::url eq doc.first.url)
        Assertions.assertEquals(currentDocument!!.signatures!!.size, 1)
        val currentSignature = currentDocument.signatures!![0]

        Assertions.assertEquals(currentSignature.originalMessage, embeddedSignature.signature.originalMessage)
        Assertions.assertEquals(currentSignature.isValid(), true)

    }

    @Test
    fun `Post signature to existing document owned by this instance`() {

        //
        // Given
        //
        val doc = makeDocument("aaabbbccc")
        dbContext.documents.save(doc.first)
        dbContext.files.insertMany(doc.second)

        val ppk = makePPK()
        val sig = doc.first.makeSignature(ppk, null, null)

        val embeddedSignature = EmbeddedSignature(listOf(), sig)

        val parsedUrl = DocumentId.parse(doc.first.url!!)

        //
        // When
        //

        val signatureFile = File.createTempFile("12323", "9999")
        signatureFile.writeText(embeddedSignature.serialize())

        val api = setupApi()

        val response = api.addSignatureToDoctagDocumentWithHttpInfo(
            parsedUrl.id,
            "abc",
            signatureFile
        )

        //
        // Then
        //
        Assertions.assertEquals(HttpStatusCode.OK, response.statusCode)
        val currentDocument = dbContext.documents.findOne(Document::url eq doc.first.url)
        Assertions.assertEquals(currentDocument!!.signatures!!.size, 1)
        val currentSignature = currentDocument.signatures!![0]

        Assertions.assertEquals(currentSignature.originalMessage, embeddedSignature.signature.originalMessage)
        Assertions.assertEquals(currentSignature.isValid(), true)

    }
}