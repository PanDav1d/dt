package de.doctag.docsrv.ui.document

import de.doctag.docsrv.i18nText
import de.doctag.docsrv.ui.active
import kweb.ElementCreator
import kweb.a
import kweb.div
import kweb.new
import kweb.plugins.fomanticUI.fomantic

enum class DocumentTabMenuActiveItem {
    DocumentList,
    SignRequests
}


fun ElementCreator<*>.documentTabMenu(activeItem: DocumentTabMenuActiveItem, rightAction: ElementCreator<*>.()->Unit)  {
    div(fomantic.ui.secondary.pointing.menu).new{
        a(fomantic.ui.item.active(activeItem == DocumentTabMenuActiveItem.DocumentList), "/documents").i18nText("ui.document.menu.document", "Dokumente")
        a(fomantic.ui.item.active(activeItem == DocumentTabMenuActiveItem.SignRequests), "/doc_sign_requests").i18nText("ui.document.menu.requests", "Anfragen")

        div(fomantic.right.menu).new {
            rightAction()
        }
    }
}