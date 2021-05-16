package de.doctag.docsrv

import appApi2
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import com.xenomachina.argparser.ArgParser
import de.doctag.docsrv.api.docsrvApi2
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.static.staticFiles
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.admin.handleInstall
import de.doctag.docsrv.ui.auth.handleLogin
import de.doctag.docsrv.ui.auth.handleLogout
import de.doctag.docsrv.ui.auth.handleRegister
import de.doctag.docsrv.ui.document.*
import de.doctag.docsrv.ui.settings.handleKeySettings
import de.doctag.docsrv.ui.settings.handleSystemSettings
import de.doctag.docsrv.ui.settings.handleUsersSettings
import de.doctag.lib.fixHttps
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.*
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.websocket.WebSockets
import kweb.*
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.reflect.KType


fun main(args: Array<String>) {
    ArgParser(args).parseInto(::DocsrvArgs).run {

        Config._instance = this

        val server = embeddedServer(Jetty, host = "0.0.0.0", port = 16097, module = Application::kwebFeature)

        server.start()


    }
}

fun Application.kwebFeature(){
    install(DefaultHeaders){
        header("Access-Control-Allow-Origin", "*")
    }
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
    install(OpenAPIGen) {
        // basic info
        info {
            version = "0.0.1"
            title = "DocServer API"
            description = "API of the Docserver"
            contact {
                name = "Doctag Team"
                email = "hallo@doctag.de"
            }
        }
        // describe the server, add as many as you want
        server("http://localhost:8080/") {
            description = "DocServer"
        }

        replaceModule(DefaultSchemaNamer, object: SchemaNamer {
            val regex = Regex("[A-Za-z0-9_.]+")
            override fun get(type: KType): String {
                return type.toString().replace(regex) { it.value.split(".").last() }.replace(Regex(">|<|, "), "_")
            }
        })
    }

    install(Kweb){
        plugins = listOf(fomanticUIPlugin)
        debug = true


        routing {
            get("/login"){
                call.respondKwebRender {
                    handleLogin()
                }
            }
        }

        buildPage = {
            logger.info("Cookie SESSION / Main is ${this.httpRequestInfo.cookies.get("SESSION")}")

            doc.head.new {
                // Not required, but recommended by HTML spec
                meta(name = "Description", content = "Dokumentenserver für signierte Dokumente")
            }
            doc.body.new {
                route {
                    //path("/login") {
                    //    handleLogin()
                    //}
                    path("/register"){
                        handleRegister(this)
                    }
                    path("/install"){
                        handleInstall(this)
                    }
                    path("/documents"){
                        handleDocumentPreviewList()
                    }
                    path("/doc_sign_requests"){
                        handleSignRequestList()
                    }
                    path("/d/{id}"){params ->
                        val docId = params.getValue("id")
                        handleDocument(docId.value,null)
                    }
                    path("/d/{id}/{hostname}"){params ->
                        val docId = params.getValue("id")
                        val hostname = params.getValue("hostname")
                        handleDocument(docId.value, hostname.value)
                    }
                    path("/doc_sign_requests/{id}"){params ->
                        val docId = params.getValue("id")
                        handleViewSignRequest(docId.value)
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
                            this.browser.navigateTo("/documents")
                        }
                    }
                    path("/logout"){
                        handleLogout()
                    }
                }
            }
        }

        apiRouting{
            docsrvApi2()
            appApi2()
        }

        routing {

            get("/openapi.json") {
                val url = db().currentConfig.hostname
                call.respond(openAPIGen.api.let{ api->
                    api.copy(servers = api.servers.map { s->s.copy(url="https://${url}".fixHttps())  }.toMutableList())
                }.serialize())
            }

            staticFiles()
        }
    }
}