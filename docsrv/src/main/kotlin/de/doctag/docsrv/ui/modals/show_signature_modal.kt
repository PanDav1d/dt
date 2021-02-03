package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.*
import de.doctag.docsrv.model.authenticatedUser
import de.doctag.docsrv.ui.modal
import de.doctag.lib.DoctagSignature
import de.doctag.lib.model.PrivatePublicKeyPair
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import java.time.Duration

fun ElementCreator<*>.showSignatureModal(key: PrivatePublicKeyPair) = modal("Signatur anzeigen"){ modal->
    div(fomantic.ui.card).new{

        val sig = DoctagSignature.make(key.privateKey!!, key.publicKey!!, Duration.ofMinutes(5), key.signingDoctagInstance!!, browser.authenticatedUser!!.let { "${it.firstName} ${it.lastName}" })

        logger.info("Created signature qr code with content ${sig.toDataString()}")

        div(fomantic.ui.image).new {
            img(attributes = mapOf("height" to "400px", "with" to "400px","src" to getQRCodeImageAsDataUrl(sig.toDataString(), 400,400)))
        }
        div(fomantic.ui.content).new{
            div().text("Von Nutzer: ${sig.signingUser}")
            div().text("Von Org: ${sig.signingDoctagInstance}/${sig.keyFingerprint}")
            div().text("Vorgang: ${sig.randomBuffer}")
            div().text("Gültigkeit: ${sig.validFromDateTime.formatDateTime()} + 5min")
        }
        div(fomantic.ui.extra.content).new {
            div().text("Signatur")
            div().text(sig.signature)
        }
    }
}