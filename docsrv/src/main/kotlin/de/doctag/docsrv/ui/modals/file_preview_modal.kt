package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.*
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv.model.authenticatedUser
import de.doctag.docsrv.ui.file
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.pdf
import de.doctag.lib.DoctagSignature
import de.doctag.lib.model.PrivatePublicKeyPair
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import java.time.Duration
import java.time.format.DateTimeFormatter



fun ElementCreator<*>.filePreviewModal(file: FileData) = modal("Vorschau"){ modal->
    div(fomantic.ui.placeholder.segment).new{

        when{
            file.contentType.isImage() -> {
                img("/f/${file._id}/download", fomantic.ui.medium.centered.image)
                div(fomantic.ui.divider.hidden)
            }
            file.contentType.isPdf() -> {
                element("iframe", mapOf("style" to "height: 70vh; width:90%; border: none", "src" to "/f/${file._id}/view"))
                div(fomantic.ui.divider.hidden)
            }
            else -> {
                div(fomantic.ui.icon.header).new {
                    i(fomantic.icon.file.pdf.outline)
                    span().text("Keine Vorschau verfügbar")
                }
            }
        }
        div(fomantic.inline).new {
            a(href = "/f/${file._id}/download", attributes = mapOf("download" to "", "class" to "ui button blue")).text("Herunterladen")
        }
    }
}

