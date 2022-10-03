package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
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

        
        pageBorderAndTitle(i18n("ui.settings.keys.pageTitle","Teilnehmerzertifikat")) {pageArea->

            val modal = addKeyModal {keyObj->
                keys.value = listOf(keyObj).plus(keys.value)
                pageArea.showToast(i18n("ui.settings.keys.addedKeySuccessMessage","Schüsselpaar hinzugefügt"), ToastKind.Success)
            }

            div(fomantic.content).new() {
                settingsTabMenu(SettingsTabMenuActiveItem.Keys) {
                    button(fomantic.ui.button.mini).i18nText("ui.settings.keys.newKey","Neuer Schlüssel").on.click {
                        modal.open()
                    }
                }

                div(fomantic.ui.divider.hidden)

                render(keys) { rKeys ->

                    logger.info("List of Keys did change")

                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().i18nText("ui.settings.keys.displayName","Anzeigename")
                                th().i18nText("ui.settings.keys.createdAt","Erstellt am")
                                th().i18nText("ui.settings.keys.createdBy","von")
                                th().new {
                                    span().i18nText("ui.settings.keys.verification","Verifizierung")
                                    i(fomantic.ui.icon.info).withPopup(null, i18n("ui.settings.keys.verificationInfoMessage","Gibt an, ob der Schlüssel bereits von DocTag verifiziert wurde"))
                                }
                                th().i18nText("ui.settings.keys.keyValidTill","Gültig bis")
                                th().i18nText("ui.settings.keys.action","Aktion")
                            }
                        }
                        tbody().new {
                            rKeys.forEach { key ->

                                val verificationStatus = when{
                                    key.verification?.signatureOfPublicKeyEntry != null  -> i18n("ui.settings.keys.yes","ja")
                                    else -> i18n("ui.settings.keys.notYetConfirmed","nicht abgeschlossen")
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

                                                pageArea.showToast(i18n("ui.settings.keys.keyUpdatedInfo","Schlüssel aktualisiert"), ToastKind.Success)
                                            }
                                            else {
                                                pageArea.showToast(i18n("ui.settings.keys.keyUpdateFailedError","Veröffentlichen fehlgeschlagen."), ToastKind.Error)
                                            }
                                        }

                                        i(fomantic.ui.remove.icon).on.click {
                                            logger.info("Removing key ${key.verboseName}")
                                            val removeModal = deleteVerifyModal(i18n("ui.settings.keys.deleteKeyObjectName","Schlüssel"), key.verboseName?:""){
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