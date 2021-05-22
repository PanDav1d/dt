import api.TestConfig
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import de.doctag.docsrv.Config
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv_api.invoker.ApiClient
import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.toSha1HexString
import org.bson.internal.Base64
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.extension.ExtendWith
import java.net.InetSocketAddress
import java.time.ZonedDateTime
import de.doctag.docsrv_api.DefaultApi
import de.doctag.docsrv_api.invoker.Configuration
import de.doctag.lib.logger
import org.litote.kmongo.save


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

fun setupApi() : DefaultApi {
    val defaultClient: ApiClient = Configuration.getDefaultApiClient()
    defaultClient.setHost("localhost")
    defaultClient.setPort(TESTING_PORT)
    defaultClient.setBasePath("")

    val apiInstance = DefaultApi(defaultClient)

    return apiInstance
}

const val DB_NAME = "docserver"

@ExtendWith(WithTestingHttpServer::class)
open class WithTestDatabase{
    companion object {
        val server = MongoServer(MemoryBackend()).also { server->
            val serverAddress: InetSocketAddress = server.bind()
            val mongoSrvAddress = ServerAddress(serverAddress).toString()

            Config._instance = TestConfig("mongodb://$mongoSrvAddress", DB_NAME)


            val config = DbContext(DB_NAME).currentConfig.apply {
                hostname = "127.0.0.1:$TESTING_PORT"
            }
            DbContext(DB_NAME).config.save(config)

            logger.info("Did setup db connection")
        }

        @JvmStatic
        @AfterAll
        internal fun cleanupAfterTest(){
            server.shutdown()
        }
    }

    val dbContext by lazy {
        DbContext(DB_NAME)
    }
}