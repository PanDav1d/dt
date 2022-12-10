package api

import SignatureInputs
import TESTING_PORT
import WithTestDatabase
import de.bwaldvogel.mongo.backend.Assert
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.AppApiClient
import makeDocument
import makePPK
import makeWorkflow
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.save
import java.time.ZonedDateTime
import org.litote.kmongo.deleteMany

private val logger = KotlinLogging.logger {}

class AppApiTest : WithTestDatabase() {

    @BeforeEach
    fun `Cleanup keys`(){
        dbContext.keys.deleteMany()
    }

    @Test
    fun `Check Session validity`() {

        //
        // Given
        //
        val doc = makeDocument("123", hostname = "127.0.0.1:$TESTING_PORT")
        dbContext.documents.save(doc.first)
        dbContext.files.insertMany(doc.second)
        dbContext.users.insertOne(User(sessions = listOf(Session("123", expires = ZonedDateTime.now().plusDays(1)))))
        dbContext.users.insertOne(User(sessions = listOf(Session("456", expires = ZonedDateTime.now().minusDays(1)))))

        //
        // Checks
        //
        Assert.equals(false, AppApiClient.checkAuthentication("127.0.0.1:$TESTING_PORT", "abc"))
        Assert.equals(true, AppApiClient.checkAuthentication("127.0.0.1:$TESTING_PORT", "123"))
        Assert.equals(false, AppApiClient.checkAuthentication("127.0.0.1:$TESTING_PORT", "456"))
    }

    @Test
    fun `Fetch prepared workflow`() {

        //
        // Given
        //
        val wf = makeWorkflow()
        val doc = makeDocument("aaabbbccc", hostname = "127.0.0.1:$TESTING_PORT", workflow = wf)
        dbContext.documents.save(doc.first)
        dbContext.files.insertMany(doc.second)
        dbContext.users.insertOne(User(sessions = listOf(Session("zzz", expires = ZonedDateTime.now().plusDays(1)))))

        val ppk = makePPK()
        dbContext.keys.insertOne(ppk)

        //
        // When
        //
        val preparedWorkflow = AppApiClient.fetchWorkflowToSign("127.0.0.1:$TESTING_PORT", "zzz", "127.0.0.1:$TESTING_PORT", doc.first._id!!)

        //
        // Then
        //
        logger.info("Available key: ${preparedWorkflow.availableKeys.joinToString { it.ppkId }}")
        Assert.equals(ppk._id, preparedWorkflow.availableKeys.single().ppkId)
        Assert.equals(wf._id, preparedWorkflow.workflow?._id)
    }

    @Test
    fun `Submit Signature`(){
        //
        // Given
        //
        val wf = makeWorkflow()
        val doc = makeDocument("acacacacac", hostname = "127.0.0.1:$TESTING_PORT", workflow = wf)
        dbContext.documents.save(doc.first)
        dbContext.files.insertMany(doc.second)
        dbContext.users.insertOne(User(sessions = listOf(Session("xxx", expires = ZonedDateTime.now().plusDays(1)))))

        val ppk = makePPK()
        dbContext.keys.insertOne(ppk)


        val inputs = SignatureInputs(
            role = "A",
            ppkId = ppk._id!!,
            inputs = listOf(WorkflowInputResult("Feld 1", "AA")),
            files = null
        )

        //
        // When
        //
        val preparedWorkflow = AppApiClient.uploadWorkflowResultAndTriggerSignature(
            "127.0.0.1:$TESTING_PORT",
            "xxx",
            "127.0.0.1:$TESTING_PORT",
            doc.first._id!!,
            inputs
        )

        //
        // Then
        //
        Assert.equals(true, preparedWorkflow!!.success)
    }
}