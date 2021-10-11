package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.addKeyModal
import de.doctag.docsrv.ui.modals.deleteVerifyModal
import de.doctag.docsrv.ui.modals.showSignatureInfoModal
import de.doctag.lib.KeyServerClient
import de.doctag.lib.loadPublicKey
import de.doctag.lib.publicKeyFingerprint
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.save

fun ElementCreator<*>.handleKeySettings(){
    authRequired {

        val keys = KVar(db().keys.find().toList())

        
        pageBorderAndTitle("Teilnehmerzertifikat") {pageArea->

            val modal = addKeyModal {keyObj->
                keys.value = listOf(keyObj).plus(keys.value)
                pageArea.showToast("Schüsselpaar hinzugefügt", ToastKind.Success)
            }

            div(fomantic.content).new() {
                settingsTabMenu(SettingsTabMenuActiveItem.Keys) {
                    button(fomantic.ui.button.mini).text("Neuer Schlüssel").on.click {
                        modal.open()
                    }
                }

                div(fomantic.ui.divider.hidden)

                render(keys) { rKeys ->

                    logger.info("List of Keys did change")

                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().text("Anzeigename")
                                th().text("Erstellt am")
                                th().text("von")
                                th().new {
                                    span().text("Verifizierung")
                                    i(fomantic.ui.icon.info).withPopup(null, "Gibt an, ob der Schlüssel bereits von DocTag verifiziert wurde")
                                }
                                th().text("Gültig bis")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rKeys.forEach { key ->

                                val verificationStatus = when{
                                    key.verification?.signatureOfPublicKeyEntry != null  -> "ja"
                                    else -> "nicht abgeschlossen"
                                }

                                tr().new {
                                    td().text(key.verboseName ?: "")
                                    td().text(key.created ?: "")
                                    td().text("${key.owner?.firstName} ${key.owner?.lastName}")
                                    td().text(verificationStatus)
                                    td().text(key.verification?.signatureValidUntil ?: "---")
                                    td().new {

                                        i(fomantic.ui.paw.icon).on.click{
                                            val signatureModal = showSignatureInfoModal(key)
                                            signatureModal.open()
                                        }

                                        i(fomantic.ui.syncAlternate.icon).on.click {
                                            val (success, keyServerResult) = KeyServerClient.loadPublicKey(key.signingDoctagInstance!!, publicKeyFingerprint(
                                                loadPublicKey(key.publicKey)!!))

                                            if(success){
                                                if(keyServerResult?.verification != null){
                                                    key.verification = keyServerResult.verification
                                                    db().keys.save(key)
                                                }

                                                pageArea.showToast("Schlüssel aktualisiert", ToastKind.Success)
                                            }
                                            else {
                                                pageArea.showToast("Veröffentlichen fehlgeschlagen.", ToastKind.Error)
                                            }
                                        }

                                        i(fomantic.ui.remove.icon).on.click {
                                            logger.info("Removing key ${key.verboseName}")
                                            val removeModal = deleteVerifyModal("Schlüssel", key.verboseName?:""){
                                                db().keys.deleteOneById(key._id!!)
                                            }
                                            removeModal.open()
                                            removeModal.onClose{
                                                keys.value = keys.value.filter { it._id != key._id }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}