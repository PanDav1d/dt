package de.doctag.docsrv.static

import io.ktor.application.call
import io.ktor.http.content.files
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.content.staticBasePackage
import io.ktor.response.respondFile
import io.ktor.routing.Routing
import io.ktor.routing.get
import java.io.File

fun Routing.staticFiles(){
    static("ressources") {
        staticBasePackage = "static"
        resources("js")
    }
}