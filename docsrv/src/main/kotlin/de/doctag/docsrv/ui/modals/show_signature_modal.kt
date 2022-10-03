package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.*
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.selectable
import de.doctag.lib.loadPublicKey
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.publicKeyFingerprint
import kweb.*
import kweb.plugins.fomanticUI.fomantic

fun ElementCreator<*>.showSignatureInfoModal(key: PrivatePublicKeyPair) = modal(i18n("ui.modals.showSignatureModal.title", "Schlüssel ${
    publicKeyFingerprint(loadPublicKey(key.publicKey)!!)} anzeigen")){ modal->


    h4(fomantic.ui.dividing.header).i18nText("ui.modals.showSignatureModal.keyHeader","Schlüssel")

    table(fomantic.ui.selectable.celled.table).new {
        tr().new {
            td().i18nText("ui.modals.showSignatureModal.fromUser","Von Nutzer")
            td().text("${key.owner.firstName} ${key.owner.lastName}")
        }

        tr().new {
            td().i18nText("ui.modals.showSignatureModal.doctagSource","Doctag System")
            td().text("${key.signingDoctagInstance}/${publicKeyFingerprint(loadPublicKey(key.publicKey)!!)}")
        }

        tr().new {
            td().i18nText("ui.modals.showSignatureModal.name1","Name 1")
            td().text("${key.ownerAddress.name1}")
        }

        tr().new {
            td().i18nText("ui.modals.showSignatureModal.name2","Name 2")
            td().text("${key.ownerAddress.name2}")
        }

        tr().new {
            td().i18nText("ui.modals.showSignatureModal.street","Straße")
            td().text("${key.ownerAddress.street}")
        }

        tr().new {
            td().i18nText("ui.modals.showSignatureModal.zipAndCity","PLZ - Ort")
            td().text("${key.ownerAddress.zipCode} - ${key.ownerAddress.city}")
        }

        tr().new {
            td().i18nText("ui.modals.showSignatureModal.countryCode","Land")
            td().text("${key.ownerAddress.countryCode}")
        }
    }

    h4(fomantic.ui.dividing.header).i18nText("ui.modals.showSignatureModal.verification","Verifikation")
    if(key.verification?.signatureOfPublicKeyEntry != null){

        table(fomantic.ui.selectable.celled.table).apply {
            setAttributeRaw("style", "table-layout: fixed;")
        }.new {
            tr().new {
                td(attributes= mapOf("style" to "width: 250px;")).text("Von")
                td(attributes= mapOf("style" to "width: 500px;overflow-wrap: break-word;")).text("${key.verification?.signedByParty}")
            }
            tr().new {
                td().i18nText("ui.modals.showSignatureModal.selectedPublicKey","Mit öffentlichem Schlüssel")
                td().new{
                    div(attributes = mapOf("style" to "font-family: monospace;overflow-wrap: break-word;"))
                        .text("${key.verification?.signedByPublicKey}")
                }
            }
            tr().new {
                td().i18nText("ui.modals.showSignatureModal.signatureOfKey","Signatur dieses Schlüssels")
                td().new{
                    div(attributes = mapOf("style" to "font-family: monospace;overflow-wrap: break-word;"))
                        .text("${key.verification?.signatureOfPublicKeyEntry}")
                }
            }
            tr().new {
                td().i18nText("ui.modals.showSignatureModal.signatureValidMessage","Signatur gültig")
                td().text(key.verifySignature().toYesNoString())
            }
            tr().new {
                td().i18nText("ui.modals.showSignatureModal.validTill","Gültig bis:")
                td().text("${key.verification?.signatureValidUntil}")
            }
            tr().new {
                td().i18nText("ui.modals.showSignatureModal.addressValid","Addresse gültig:")
                td().text("${key.verification?.isAddressVerified?.toYesNoString()}")
            }
            tr().new {
                td().i18nText("ui.modals.showSignatureModal.doctagHostValid","DocTag Host gültig:")
                td().text("${key.verification?.isSigningDoctagInstanceVerified?.toYesNoString()}")
            }
        }
    }
    else {
        div().i18nText("ui.modals.showSignatureModal.notVerified","Nicht verifiziert")
    }
}