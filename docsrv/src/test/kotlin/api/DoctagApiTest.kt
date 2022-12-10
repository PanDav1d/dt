package api

import TESTING_PORT
import WithTestDatabase
import com.github.salomonbrys.kotson.fromJson
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.Assert
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import de.doctag.docsrv.Config
import de.doctag.docsrv.DocSrvConfig
import de.doctag.docsrv.kwebFeature
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kweb.util.gson
import makeDocument
import makePPK
import org.bson.internal.Base64
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


class DoctagApiTest : WithTestDatabase() {


    @Test
    fun `Fetch Document Test`() {

        //
        // Given
        //
        val doc = makeDocument("123", hostname = "127.0.0.1:$TESTING_PORT")
        dbContext.documents.save(doc.first)
        dbContext.files.insertMany(doc.second)


        //
        // When
        //

        val actualDoc = DocServerClient.loadDocument(doc.first.url!!)

        //
        // Then
        //
        Assert.notNull(actualDoc)
        val acutalAttachment = actualDoc?.files?.find { it._id  == actualDoc?.document?.attachmentId }
        Assert.equals(acutalAttachment?.base64Content, Base64.encode("123".toByteArray()))
    }

    @Test
    fun `Post signature to existing document owned by this instance`() {

        //
        // Given
        //
        val doc = makeDocument("aaabbbccc", hostname = "127.0.0.1:$TESTING_PORT")
        dbContext.documents.save(doc.first)
        dbContext.files.insertMany(doc.second)

        val ppk = makePPK()
        val sig = doc.first.makeSignature(ppk, null, null, "none")

        val embeddedSignature = EmbeddedSignature(listOf(), sig)

        val parsedUrl = DocumentId.parse(doc.first.url!!)

        //
        // When
        //
        val result = DocServerClient.pushSignature("http://127.0.0.1:${TESTING_PORT}/d/${parsedUrl.id}", embeddedSignature)


        //
        // Then
        //
        Assertions.assertEquals(true, result)
        val currentDocument = dbContext.documents.findOne(Document::url eq doc.first.url)
        Assertions.assertEquals(currentDocument!!.signatures!!.size, 1)
        val currentSignature = currentDocument.signatures!![0]

        Assertions.assertEquals(currentSignature.originalMessage, embeddedSignature.signature.originalMessage)
        Assertions.assertEquals(currentSignature.isValid(), true)
    }
}