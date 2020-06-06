package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.model.*
import de.doctag.docsrv.ui.forms.keyAddForm
import de.doctag.docsrv.ui.modal
import de.doctag.lib.KeyServerClient
import de.doctag.lib.model.Person
import de.doctag.lib.model.PrivatePublicKeyPair
import kweb.ElementCreator
import kweb.logger
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save
import kweb.*

fun ElementCreator<*>.addKeyModal(onKeyAdd: (u: PrivatePublicKeyPair)->Unit) = modal("Schlüssel hinzufügen"){ modal->
    val hasError = KVar<String?>(null)

    render(hasError){ errorMessage->
        errorMessage?.let{
            div(fomantic.ui.message).new {
                div(fomantic.ui.header).text("Fehler beim Anlegen")
                p().text(errorMessage!!)
            }
        }
    }

    keyAddForm { verboseName, address ->
        hasError.value = null
        logger.info("Creating ppk pair with verboseName ${verboseName}")

        val usr = browser.authenticatedUser!!
        val owner = Person(
            userId = usr._id,
            firstName = usr.firstName,
            lastName = usr.lastName,
            email = usr.emailAdress
        )

        val ppk = PrivatePublicKeyPair.make(verboseName, Config.instance.hostName, address, owner)

        logger.info("Saving PPK to db")


        ppk.apply {
            DbContext.keys.save(ppk)
        }

        logger.info("Publishing PPK")
        val (success, msg) = KeyServerClient.publishPublicKey(ppk)

        if(success) {
            onKeyAdd(ppk)
            modal.close()
        }
        else {
            hasError.value = msg
        }
    }
}