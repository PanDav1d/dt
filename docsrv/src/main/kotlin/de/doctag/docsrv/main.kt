package de.doctag.docsrv

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.xenomachina.argparser.ArgParser
import de.doctag.docsrv.api.docsrvApi
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.static.staticFiles
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.admin.handleInstall
import de.doctag.docsrv.ui.auth.handleLogin
import de.doctag.docsrv.ui.auth.handleLogout
import de.doctag.docsrv.ui.auth.handleRegister
import de.doctag.docsrv.ui.document.handleDocument
import de.doctag.docsrv.ui.document.handleDocumentList
import de.doctag.docsrv.ui.document.handleSignRequestList
import de.doctag.docsrv.ui.settings.handleKeySettings
import de.doctag.docsrv.ui.settings.handleSystemSettings
import de.doctag.docsrv.ui.settings.handleUsersSettings
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
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import java.time.Duration



fun main(args: Array<String>) {
    ArgParser(args).parseInto(::DocsrvArgs).run {
        Config._instance = this
        embeddedServer(Jetty, host = "0.0.0.0", port = 16097, module = Application::kwebFeature).start()
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
        exception<BadRequest>{err ->
            call.respond(HttpStatusCode.BadRequest, err.message?:"")
        }
        exception<NotFound>{err ->
            call.respond(HttpStatusCode.NotFound, err.message?:"")
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
                meta(name = "Description", content = "Dokumentenserver für signierte Dokumente")
            }
            doc.body.new {
                route {
                    path("/login") {
                        handleLogin()
                    }
                    path("/register"){
                        handleRegister(this)
                    }
                    path("/install"){
                        handleInstall(this)
                    }
                    path("/documents"){
                        handleDocumentList()
                    }
                    path("/doc_sign_requests"){
                        handleSignRequestList()
                    }
                    path("/d/{id}"){params ->
                        val docId = params.getValue("id")
                        handleDocument(docId.value)
                    }
                    path("/settings/users"){
                        handleUsersSettings()
                    }
                    path("/settings/keys"){
                        handleKeySettings()
                    }
                    path("/settings/system/"){params->
                        handleSystemSettings(KVar("host"))
                    }
                    path("/settings/system/{item}"){params->
                        handleSystemSettings(params.get("item") ?: KVar("host"))
                    }
                    path("/") {
                        authRequired {
                            pageBorderAndTitle("This is the title") {
                                div(fomantic.content).new() {
                                    p().text("Hello test")
                                    p().text("Session ${this.browser.httpRequestInfo.cookies.get("SESSION")}")
                                }
                            }
                        }
                    }
                    path("/logout"){
                        handleLogout()
                    }
                }
            }
        }
        routing {
            docsrvApi()
            staticFiles()
        }
    }
}


