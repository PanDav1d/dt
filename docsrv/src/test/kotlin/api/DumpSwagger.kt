package api

import WithTestDatabase
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


class DumpSwagger : WithTestDatabase() {


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