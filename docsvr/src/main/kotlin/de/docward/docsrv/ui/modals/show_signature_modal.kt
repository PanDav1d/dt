package de.docward.docsrv.ui.modals

import de.docward.docsrv.*
import de.docward.docsrv.model.PrivatePublicKeyPair
import de.docward.docsrv.model.authenticatedUser
import de.docward.docsrv.ui.modal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import java.time.Duration
import java.time.format.DateTimeFormatter

fun ElementCreator<*>.showSignatureModal(key:PrivatePublicKeyPair) = modal("Signatur anzeigen"){ modal->
    div(fomantic.ui.card).new{

        val sig = DocSignature.make(loadPrivateKey(key.privateKey)!!, loadPublicKey(key.publicKey)!!, Duration.ofMinutes(5), key.signingParty!!, browser.authenticatedUser!!.let { "${it.firstName} ${it.lastName}" })

        logger.info("Created signature qr code with content ${sig.toDataString}")

        div(fomantic.ui.image).new {
            img(attributes = mapOf("height" to "400px", "with" to "400px","src" to getQRCodeImageAsDataUrl(sig.toDataString, 400,400)))
        }
        div(fomantic.ui.content).new{
            div().text("Von Nutzer: ${sig.signingUser}")
            div().text("Von Org: ${sig.signingParty}/${sig.keyFingerprint}")
            div().text("Vorgang: ${sig.randomBuffer}")
            div().text("Gültigkeit: ${DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(sig.validFromDateTime)} + 5min")
        }
        div(fomantic.ui.extra.content).new {
            div().text("Signatur")
            div().text(sig.signature)
        }
    }
}