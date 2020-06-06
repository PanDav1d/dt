package de.docward.docsrv.ui.modals

import de.docward.docsrv.keysrv_api.KeyServer
import de.docward.docsrv.model.*
import de.docward.docsrv.savePrivateKey
import de.docward.docsrv.savePublicKey
import de.docward.docsrv.ui.forms.keyAddForm
import de.docward.docsrv.ui.modal
import kweb.ElementCreator
import kweb.logger
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.time.ZonedDateTime
import kweb.*

fun ElementCreator<*>.addKeyModal(onKeyAdd: (u:PrivatePublicKeyPair)->Unit) = modal("Schlüssel hinzufügen"){ modal->
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
        val (success, msg) = KeyServer.publishPublicKey(ppk)

        if(success) {
            onKeyAdd(ppk)
            modal.close()
        }
        else {
            hasError.value = msg
        }
    }
}