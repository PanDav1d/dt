package de.doctag.docsrv.ui.document

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
        a(fomantic.ui.item.active(activeItem == DocumentTabMenuActiveItem.DocumentList), "/documents").text("Dokumente")
        a(fomantic.ui.item.active(activeItem == DocumentTabMenuActiveItem.SignRequests), "/doc_sign_requests").text("Anfragen")



        div(fomantic.right.menu).new {
            rightAction()
        }

    }
}