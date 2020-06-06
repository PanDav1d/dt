package de.docward.docsrv.ui.auth

import de.docward.docsrv.model.clearSession
import de.docward.docsrv.ui.centeredBox
import de.docward.docsrv.ui.navigateTo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*

fun ElementCreator<*>.handleLogout(){
    this.browser.clearSession()
    centeredBox {
        h2().text("Abgemeldet")
        span().text("Sie haben sich abgemeldet. Weiter zur ")
        a(href = "/login").text("Anmeldung")

        p().text("Sie werden automatisch in 5 Sekunden weitergeleitet.")
        GlobalScope.launch {
            delay(5000)
            browser.navigateTo("/login")
        }
    }
}