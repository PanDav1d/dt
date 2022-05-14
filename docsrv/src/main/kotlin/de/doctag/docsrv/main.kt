package de.doctag.docsrv

import appRoutes
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.xenomachina.argparser.ArgParser
import de.doctag.docsrv.api.docServerApi
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.static.staticFiles
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.admin.handleInstall
import de.doctag.docsrv.ui.auth.handleLogin
import de.doctag.docsrv.ui.auth.handleLogout
import de.doctag.docsrv.ui.auth.handleRegister
import de.doctag.docsrv.ui.document.*
import de.doctag.docsrv.ui.document.components.DocumentViewActiveItem
import de.doctag.docsrv.ui.settings.handleKeySettings
import de.doctag.docsrv.ui.settings.handleSystemSettings
import de.doctag.docsrv.ui.settings.handleUsersSettings
import ktor.swagger.SwaggerSupport
import io.ktor.application.*
import io.ktor.content.*
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.*
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.jackson.jackson
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.websocket.WebSockets
import ktor.swagger.version.shared.Contact
import ktor.swagger.version.shared.Information
import ktor.swagger.version.v3.OpenApi
import kweb.*
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import java.lang.Exception
import java.time.Duration


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
    install(Locations)
    install(Compression)
    install(ContentNegotiation) {
        jackson {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
        status(HttpStatusCode.NotFound) { notFound ->
            logger.error("Not found ${call.request.httpMethod.value} ${call.request.path()}")

            call.respond(TextContent("${notFound.value} ${notFound.description}. Request path was ${call.request.path()}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), notFound))
        }

        exception<Exception>{err->
            de.doctag.lib.logger.error("Request failed with error (${err.javaClass.name}): ${err.message}")
            logger.error(err.stackTraceToString())
            call.respond(HttpStatusCode.InternalServerError, err.message?:"")
        }

    }
    install(WebSockets){
        pingPeriod = Duration.ofSeconds(10)
        timeout = Duration.ofSeconds(30)
    }
    install(SwaggerSupport) {
        forwardRoot = false
        val information = Information(
            version = "0.1",
            title = "DocServer",
            description = "More Info available on https://www.doctag.de",
            contact = Contact(
                name = "Frank Englert",
                url = "https://www.doctag.de"
            )
        )
        openApi = OpenApi().apply {
            info = information
        }
    }

    install(Kweb){
        plugins = listOf(fomanticUIPlugin,NoZoomPlugin())
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
                        handleDocument(docId.value,null, DocumentViewActiveItem.PREVIEW.name)
                    }
                    path("/d/{id}/view/{page}"){params ->
                        val docId = params.getValue("id")
                        val page = params.getValue("page")
                        handleDocument(docId.value, null, page.value)
                    }
                    path("/d/{id}/{hostname}"){params ->
                        val docId = params.getValue("id")
                        val hostname = params.getValue("hostname")
                        handleDocument(docId.value, hostname.value, DocumentViewActiveItem.PREVIEW.name)
                    }
                    path("/d/{id}/{hostname}/view/{page}"){params ->
                        val docId = params.getValue("id")
                        val hostname = params.getValue("hostname")
                        val page = params.getValue("page")
                        handleDocument(docId.value, hostname.value, page.value)
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


        routing {

            docServerApi()
            appRoutes()


            staticFiles()

            //trace { application.log.info(it.buildText()) }
        }
    }
}