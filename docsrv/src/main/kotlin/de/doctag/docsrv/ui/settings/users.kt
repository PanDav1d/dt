package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.formatDate
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.UserEditAction
import de.doctag.docsrv.ui.modals.addUserModal
import de.doctag.docsrv.ui.modals.editUserModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
fun ElementCreator<*>.handleUsersSettings(){
    authRequired {

        val users = KVar(db().users.find().toList())

        
        pageBorderAndTitle(i18n("ui.settings.users.pageTitle","Einstellungen")) {pageArea->

            val modal = addUserModal {userObj->
                users.value = listOf(userObj).plus(users.value)
                pageArea.showToast(i18n("ui.settings.users.userAddedSuccessMessage","Benutzer hinzugefügt"), ToastKind.Success)
            }

            div(fomantic.content).new() {
                settingsTabMenu(SettingsTabMenuActiveItem.User) {
                    button(fomantic.ui.button.mini).i18nText("ui.settings.users.newUser","Neuer Benutzer").on.click {
                        modal.open()
                    }
                }


                div(fomantic.ui.divider.hidden)

                render(users) { rUsers ->

                    logger.info("List of users did change")

                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().i18nText("ui.settings.users.firstName","Vorname")
                                th().i18nText("ui.settings.users.lastName","Nachname")
                                th().i18nText("ui.settings.users.email","E-Mail")
                                th().i18nText("ui.settings.users.created","Erstellt am")
                                th().i18nText("ui.settings.users.isAdmin","Admin?")
                                th().i18nText("ui.settings.users.actions","Aktion")
                            }
                        }
                        tbody().new {
                            rUsers.forEach { user ->

                                tr().new {
                                    td().text(user.firstName ?: "")
                                    td().text(user.lastName ?: "")
                                    td().text(user.emailAdress ?: "")
                                    td().text(user.created?.formatDate() ?: "")
                                    td().text(if(user.isAdmin!=false){"✓"} else "-")
                                    td().new {

                                        i(fomantic.ui.edit.icon).on.click {
                                            logger.info("Editing user ${user.firstName} ${user.lastName}")

                                            val editModal = editUserModal(user){ user, action ->
                                                when(action){
                                                    UserEditAction.UserDeleted -> {
                                                        users.value = users.value.filter { it._id!=user._id }
                                                        pageArea.showToast(i18n("ui.settings.users.userDeletedMessage","Benutzer entfernt"), ToastKind.Success)
                                                    }
                                                    UserEditAction.PasswordChanged -> {
                                                        pageArea.showToast(i18n("ui.settings.users.passwordChangedMessage","Passwort geändert"), ToastKind.Success)
                                                    }
                                                    UserEditAction.UserModified -> {
                                                        users.value = users.value.map{
                                                            if(it._id==user._id) user else it
                                                        }
                                                        pageArea.showToast(i18n("ui.settings.users.userModifiedMessage","Benutzer bearbeitet"), ToastKind.Success)
                                                    }
                                                }
                                            }
                                            editModal.open()
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