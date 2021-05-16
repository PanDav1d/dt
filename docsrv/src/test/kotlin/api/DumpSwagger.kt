package api

import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import de.doctag.docsrv.Config
import de.doctag.docsrv.kwebFeature
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.io.File
import java.net.InetSocketAddress


class DumpSwagger {

    companion object {
        val server = MongoServer(MemoryBackend()).also { server->
            val serverAddress: InetSocketAddress = server.bind()
            val mongoSrvAddress = ServerAddress(serverAddress).toString()

            Config._instance = TestConfig("mongodb://$mongoSrvAddress", DB_NAME)
        }

        @JvmStatic
        @AfterAll
        internal fun cleanupAfterTest(){
            server.shutdown()
        }
    }

    @Test
    fun `Dump Swagger definition`(){


        withTestApplication(Application::kwebFeature) {
            with(handleRequest(HttpMethod.Get, "openapi.json")){
                val fd = File("../docserver_openapi.json")
                fd.writeText(response.content!!)
            }
        }
    }
}