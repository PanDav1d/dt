package de.doctag.keysrv.ui.modals

import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.User
import de.doctag.keysrv.ui.forms.userAddForm
import de.doctag.keysrv.ui.modal
import de.doctag.keysrv.ui.tertiary
import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import de.doctag.lib.model.PrivatePublicKeyPair
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import java.time.ZonedDateTime

fun ElementCreator<*>.keyGeneratorModal() = modal("Schlüsselpaar erzeugen"){ modal->

    val kvKeyPair = KVar(PrivatePublicKeyPair.make("abc", "keyserver", Address(), Person()))

    render(kvKeyPair){ keyPair ->
        h3().text("Öffentlicher Schlüssel")
        div(attributes = mapOf("style" to "font-family: monospace;overflow-wrap: break-word;")).text(keyPair.publicKey?:"---")

        h3().text("Privater Schlüssel")
        div(attributes = mapOf("style" to "font-family: monospace;overflow-wrap: break-word;")).text(keyPair.privateKey?:"---")
    }

    button(fomantic.ui.button.inline).apply {
        text("Schließen")
    }.on.click {
        modal.close()
    }

    button(fomantic.ui.button.inline.tertiary).apply {
        text("Neu erzeugen")
    }.on.click {
        kvKeyPair.value = PrivatePublicKeyPair.make("abc", "keyserver", Address(), Person())
    }
}