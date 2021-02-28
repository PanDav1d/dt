package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.*
import de.doctag.docsrv.model.authenticatedUser
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.selectable
import de.doctag.lib.DoctagSignature
import de.doctag.lib.loadPublicKey
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.publicKeyFingerprint
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import java.time.Duration

fun ElementCreator<*>.showSignatureInfoModal(key: PrivatePublicKeyPair) = modal("Schlüssel ${
    publicKeyFingerprint(loadPublicKey(key.publicKey)!!)} anzeigen"){ modal->


    h4(fomantic.ui.dividing.header).text("Schlüssel")

    table(fomantic.ui.selectable.celled.table).new {
        tr().new {
            td().text("Von Nutzer")
            td().text("${key.owner.firstName} ${key.owner.lastName}")
        }

        tr().new {
            td().text("Doctag System")
            td().text("${key.signingDoctagInstance}/${publicKeyFingerprint(loadPublicKey(key.publicKey)!!)}")
        }

        tr().new {
            td().text("Name 1")
            td().text("${key.ownerAddress.name1}")
        }

        tr().new {
            td().text("Name 2")
            td().text("${key.ownerAddress.name2}")
        }

        tr().new {
            td().text("Straße")
            td().text("${key.ownerAddress.street}")
        }

        tr().new {
            td().text("PLZ - Ort")
            td().text("${key.ownerAddress.zipCode} - ${key.ownerAddress.city}")
        }

        tr().new {
            td().text("Land")
            td().text("${key.ownerAddress.countryCode}")
        }
    }

    h4(fomantic.ui.dividing.header).text("Verifikation")
    if(key.verification?.signatureOfPublicKeyEntry != null){

        table(fomantic.ui.selectable.celled.table).apply {
            setAttributeRaw("style", "table-layout: fixed;")
        }.new {
            tr().new {
                td(attributes= mapOf("style" to "width: 250px;")).text("Von")
                td(attributes= mapOf("style" to "width: 500px;overflow-wrap: break-word;")).text("${key.verification?.signedByParty}")
            }
            tr().new {
                td().text("Mit öffentlichem Schlüssel")
                td().new{
                    div(attributes = mapOf("style" to "font-family: monospace;overflow-wrap: break-word;"))
                        .text("${key.verification?.signedByPublicKey}")
                }
            }
            tr().new {
                td().text("Signatur dieses Schlüssels")
                td().new{
                    div(attributes = mapOf("style" to "font-family: monospace;overflow-wrap: break-word;"))
                        .text("${key.verification?.signatureOfPublicKeyEntry}")
                }
            }
            tr().new {
                td().text("Signatur gültig")
                td().text(key.verifySignature().toYesNoString())
            }
            tr().new {
                td().text("Gültig bis:")
                td().text("${key.verification?.signatureValidUntil}")
            }
            tr().new {
                td().text("Addresse gültig:")
                td().text("${key.verification?.isAddressVerified?.toYesNoString()}")
            }
            tr().new {
                td().text("DocTag Host gültig:")
                td().text("${key.verification?.isSigningDoctagInstanceVerified?.toYesNoString()}")
            }
        }
    }
    else {
        div().text("Nicht verifiziert")
    }
}