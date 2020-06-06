package de.doctag.keysrv


import Config
import KeySrvConfig
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import com.mongodb.ServerAddress
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import de.doctag.keysrv.GsonHelper
import de.doctag.keysrv.kwebFeature
import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.PublicKeyEntry
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import junit.framework.Assert.assertEquals
import java.net.InetSocketAddress
import java.time.ZonedDateTime


class TestConfig(override val dbConnection: String, override val dbName: String) : KeySrvConfig


class AddPrivateKeyTests: StringSpec() {

    val server = MongoServer(MemoryBackend()).also {server->
        val serverAddress: InetSocketAddress = server.bind()
        val mongoSrvAddress = ServerAddress(serverAddress).toString()

        Config._instance = TestConfig("mongodb://$mongoSrvAddress", "keysrv")
    }


    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)

        server.shutdown()
    }

    init {
        "Test add public key" {
            val test = withTestApplication(Application::kwebFeature) {
                with(handleRequest(HttpMethod.Post, "/pk/docsrv.englert.xyz"){
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader("X-Message-Signature", "MEYCIQDnH1bJgqlh6WajoI9U60041c9x+EFfgxhGcVrje4Xn1QIhALpX9tv48F34pjd+9F9hJo3ms2tCsTO040G7I4wUU9Yu")
                    setBody("""{"publicKey":"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjzFe3IuPhq3uTKAvkWlRxP+r0xeZYRxzLBQiEGAPcEDZaKG7SoneA7CTCf+0rjjjyZhBA/uVRtnwaG5jrFawlw\u003d\u003d","verboseName":"die 4.te","owner":{"userId":"5e9759eb7c44946335a65387","firstName":"Frank","lastName":"Englert","email":"f.englert@gmail.com"},"issuer":{"name1":"Frank ENGLERT","name2":"Frank ENGLERT","street":"ELISABETH-HATTEMER-STRAßE 4","city":"Darmstadt","zipCode":"64289","countryCode":"DE"},"signingParty":"127.0.0.1"}""")
                }) {
                    assertSoftly {
                        assertEquals(HttpStatusCode.OK, response.status())
                        val actualKeys = DbContext.publicKeys.find().toList()
                        actualKeys.count() shouldBe 1
                        actualKeys[0].fingerpint shouldBe "1d4688fb"
                        actualKeys[0].signingParty shouldBe "docsrv.englert.xyz"
                    }
                }

                with(handleRequest(HttpMethod.Get, "/pk/docsrv.englert.xyz/1d4688fb")){
                    response.status() shouldBe HttpStatusCode.OK

                    val gsonW = GsonBuilder().registerTypeAdapter(ZonedDateTime::class.java, GsonHelper.ZDT_DESERIALIZER).setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").create()
                    val certificate = gsonW.fromJson<PublicKeyEntry>(response.content!!)

                    certificate.issuer.city shouldBe "Darmstadt"
                    certificate.issuer.countryCode shouldBe "DE"
                    certificate.issuer.name1 shouldBe "Frank ENGLERT"
                    certificate.issuer.zipCode shouldBe "64289"
                    certificate.issuer.street shouldBe "ELISABETH-HATTEMER-STRAßE 4"

                    certificate.owner.firstName shouldBe "Frank"
                    certificate.owner.lastName shouldBe "Englert"
                }
            }
        }
    }
}