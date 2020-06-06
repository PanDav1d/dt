package de.doctag.docsrv.ui.auth

import de.doctag.docsrv.ui.centeredBox
import kweb.ElementCreator
import kweb.WebBrowser
import kweb.h2

fun WebBrowser.handleRegister(content: ElementCreator<*>) {

    content.centeredBox {
        h2().text("Account erstellen")
    }
}