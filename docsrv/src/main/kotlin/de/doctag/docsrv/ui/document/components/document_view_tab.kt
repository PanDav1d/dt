package de.doctag.docsrv.ui.document.components

import de.doctag.docsrv.ui.active
import kweb.ElementCreator
import kweb.a
import kweb.div
import kweb.new
import kweb.plugins.fomanticUI.fomantic

enum class DocumentViewActiveItem {
    PREVIEW,
    DETAILS
}


fun ElementCreator<*>.documentViewTabMenu(documentId: String?, host: String?, activeItem: DocumentViewActiveItem) {
    div(fomantic.ui.secondary.vertical.menu).new {
        a(
            fomantic.ui.item.active(activeItem == DocumentViewActiveItem.PREVIEW),
            host?.let{"/d/${documentId}/$host/view/preview"} ?: "/d/${documentId}/view/preview"
        ).text("Dokument")
        a(
            fomantic.ui.item.active(activeItem == DocumentViewActiveItem.DETAILS),
            host?.let{"/d/${documentId}/$host/view/details"} ?: "/d/${documentId}/view/details"
        ).text("Signaturen")
    }
}