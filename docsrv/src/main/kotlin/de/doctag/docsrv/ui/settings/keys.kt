package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.addKeyModal
import de.doctag.docsrv.ui.modals.showSignatureModal
import de.doctag.lib.KeyServerClient
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import java.time.format.DateTimeFormatter

fun ElementCreator<*>.handleKeySettings(){
    authRequired {

        val keys = KVar(DbContext.keys.find().toList())

        
        pageBorderAndTitle("Schlüssel") {pageArea->

            val modal = addKeyModal {keyObj->
                keys.value = listOf(keyObj).plus(keys.value)
                pageArea.showToast("Schüsselpaar hinzugefügt", ToastKind.Success)
            }

            div(fomantic.content).new() {
                div(fomantic.ui.secondary.pointing.menu).new{
                    a(fomantic.ui.item, "/settings/users").text("Benutzer")
                    a(fomantic.ui.item.active, "/settings/keys").text("Schlüssel")
                }

                button(fomantic.ui.button).text("Neuer Schlüssel").on.click {
                    modal.open()
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
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rKeys.forEach { key ->

                                tr().new {
                                    td().text(key.verboseName ?: "")
                                    td().text(key.created?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "")
                                    td().text("${key.owner?.firstName} ${key.owner?.lastName}")
                                    td().new {

                                        i(fomantic.ui.paw.icon).on.click{
                                            val signatureModal = showSignatureModal(key)
                                            signatureModal.open()
                                        }

                                        i(fomantic.ui.upload.icon).on.click {
                                            val (success, msg) = KeyServerClient.publishPublicKey(key)
                                            if(success){
                                                pageArea.showToast("Schlüssel veröffentlicht", ToastKind.Success)
                                            }
                                            else {
                                                pageArea.showToast("Veröffentlichen fehlgeschlagen.", ToastKind.Error)
                                            }
                                        }

                                        i(fomantic.ui.edit.icon).on.click {
                                            logger.info("Editing key ${key.verboseName}")

                                            /*val editModal = editUserModal(user){ user, action ->
                                                when(action){
                                                    UserEditAction.UserDeleted -> {
                                                        users.value = users.value.filter { it._id!=user._id }
                                                        pageArea.showToast("Benutzer entfernt", ToastKind.Success)
                                                    }
                                                    UserEditAction.UserModified -> {
                                                        users.value = users.value.map{
                                                            if(it._id==user._id) user else it
                                                        }
                                                        pageArea.showToast("Benutzer bearbeitet", ToastKind.Success)
                                                    }
                                                }
                                            }
                                            editModal.open()*/
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