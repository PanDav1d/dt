package de.doctag.keysrv

import Config
import KeySrvConfig
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.PublicKeyEntry
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.time.ZonedDateTime


class TestConfig(override val dbConnection: String, override val dbName: String) : KeySrvConfig


class AddPrivateKeyTests() {



    companion object {
        val server = MongoServer(MemoryBackend()).also {server->
            val serverAddress: InetSocketAddress = server.bind()
            val mongoSrvAddress = ServerAddress(serverAddress).toString()

            Config._instance = TestConfig("mongodb://$mongoSrvAddress", "keysrv")
        }

        @JvmStatic
        @AfterAll
        internal fun cleanupAfterTest(){
            server.shutdown()
        }
    }



    @Test
    fun `Test add public key`() {
        val test = withTestApplication(Application::kwebFeature) {
            with(handleRequest(HttpMethod.Post, "/pk/docsrv.englert.xyz"){
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-Message-Signature", "MEYCIQDnH1bJgqlh6WajoI9U60041c9x+EFfgxhGcVrje4Xn1QIhALpX9tv48F34pjd+9F9hJo3ms2tCsTO040G7I4wUU9Yu")
                setBody("""{"publicKey":"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjzFe3IuPhq3uTKAvkWlRxP+r0xeZYRxzLBQiEGAPcEDZaKG7SoneA7CTCf+0rjjjyZhBA/uVRtnwaG5jrFawlw\u003d\u003d","verboseName":"die 4.te","owner":{"userId":"5e9759eb7c44946335a65387","firstName":"Frank","lastName":"Englert","email":"f.englert@gmail.com"},"ownerAddress":{"name1":"Frank ENGLERT","name2":"Frank ENGLERT","street":"ELISABETH-HATTEMER-STRAßE 4","city":"Darmstadt","zipCode":"64289","countryCode":"DE"},"signingDoctagInstance":"127.0.0.1"}""")
            }) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                    val actualKeys = DbContext.publicKeys.find().toList()
                Assertions.assertEquals(actualKeys.count(), 1)
                Assertions.assertEquals(actualKeys[0].fingerpint, "1d4688fb")
                Assertions.assertEquals(actualKeys[0].signingDoctagInstance, "docsrv.englert.xyz")
            }


            with(handleRequest(HttpMethod.Get, "/pk/docsrv.englert.xyz/1d4688fb")){
                Assertions.assertEquals(response.status(), HttpStatusCode.OK)

                val gsonW = GsonBuilder().registerTypeAdapter(ZonedDateTime::class.java, GsonHelper.ZDT_DESERIALIZER).setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").create()
                val certificate = gsonW.fromJson<PublicKeyEntry>(response.content!!)

                Assertions.assertEquals(certificate.ownerAddress.city , "Darmstadt")
                Assertions.assertEquals(certificate.ownerAddress.countryCode , "DE")
                Assertions.assertEquals(certificate.ownerAddress.name1 , "Frank ENGLERT")
                Assertions.assertEquals(certificate.ownerAddress.zipCode , "64289")
                Assertions.assertEquals(certificate.ownerAddress.street , "ELISABETH-HATTEMER-STRAßE 4")

                Assertions.assertEquals(certificate.owner.firstName , "Frank")
                Assertions.assertEquals(certificate.owner.lastName , "Englert")
            }
        }
    }
}