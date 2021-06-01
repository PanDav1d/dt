package de.doctag.docsrvHostingController

import DocSrvConfigImpl
import DocSrvHostingControllerConfig
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.xenomachina.argparser.ArgParser
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.jackson.jackson
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.websocket.WebSockets
import kweb.*
import kweb.plugins.fomanticUI.fomanticUIPlugin
import java.time.Duration



fun main() {

    de.doctag.docsrv.Config._instance = DocSrvConfigImpl(Config.instance.dbConnection, Config.instance.docSrvDbNameTemplate)
    embeddedServer(Jetty, host = "0.0.0.0", port = 16096, module = Application::kwebFeature).start()

}

fun Application.kwebFeature(){
    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) {
        jackson {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            registerModule(JavaTimeModule())
        }
    }
    install(WebSockets){
        pingPeriod = Duration.ofSeconds(10)
        timeout = Duration.ofSeconds(30)
    }
    install(Kweb){
        plugins = listOf(fomanticUIPlugin)
        debug = true
        buildPage = {
            logger.info("Cookie SESSION / Main is ${this.httpRequestInfo.cookies.get("SESSION")}")

            doc.head.new {
                // Not required, but recommended by HTML spec
                meta(name = "Description", content = "Hosting-Controller zum Aufsetzen neuer Instanzen")
            }
            doc.body.new {

                route {
                    path("/"){
                        browser.url.value = "/createInstance"
                    }
                    path("/createInstance"){
                        handleCreateInstance(this)
                    }
                }
            }
        }
    }
}


