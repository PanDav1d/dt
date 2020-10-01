package de.doctag.keysrv

import KeySrvArgs
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.xenomachina.argparser.ArgParser
import de.doctag.keysrv.api.publicKeys
import de.doctag.keysrv.ui.*
import de.doctag.keysrv.ui.auth.handleLogin
import de.doctag.keysrv.ui.auth.handleLogout
import de.doctag.keysrv.ui.settings.handleKeySettings
import de.doctag.keysrv.ui.settings.handleUsersSettings
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.websocket.WebSockets
import kweb.*
import kweb.plugins.fomanticUI.fomanticUIPlugin
import java.time.Duration


fun main(args: Array<String>) {
    ArgParser(args).parseInto(::KeySrvArgs).run {
        Config._instance = this

        embeddedServer(Jetty, host = "127.0.0.1", port = 16098, module = Application::kwebFeature).start()
    }
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
    install(StatusPages){
        exception<BadRequest>{ err ->
            call.respond(HttpStatusCode.BadRequest, err.message?:"")
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
                meta(name = "Description", content = "Schlüsselserver für zum Abruf von Signaturen")
            }
            doc.body.new {
                route {
                    path("/login") {
                        handleLogin()
                    }
                    path("/settings/users"){
                        handleUsersSettings()
                    }
                    path("/settings/keys"){
                        handleKeySettings()
                    }
                    path("/") {
                        browser.navigateTo("/settings/keys")
                    }
                    path("/logout"){
                        handleLogout()
                    }
                }
            }
        }
        routing {
            publicKeys()
        }
    }
}


