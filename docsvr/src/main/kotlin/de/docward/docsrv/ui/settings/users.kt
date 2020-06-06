package de.docward.docsrv.ui.settings

import de.docward.docsrv.generatePasswordHash
import de.docward.docsrv.model.DbContext
import de.docward.docsrv.model.User
import de.docward.docsrv.model.authRequired
import de.docward.docsrv.ui.*
import de.docward.docsrv.ui.forms.userAddForm
import de.docward.docsrv.ui.forms.userDeleteForm
import de.docward.docsrv.ui.forms.userEditForm
import de.docward.docsrv.ui.forms.userPasswordEditForm
import de.docward.docsrv.ui.modals.UserEditAction
import de.docward.docsrv.ui.modals.addUserModal
import de.docward.docsrv.ui.modals.editUserModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.eq
import org.litote.kmongo.replaceOneById
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun ElementCreator<*>.handleUsersSettings(){
    authRequired {

        val users = KVar(DbContext.users.find().toList())

        
        pageBorderAndTitle("Einstellungen") {pageArea->

            val modal = addUserModal {userObj->
                users.value = listOf(userObj).plus(users.value)
                pageArea.showToast("Benutzer hinzugefügt", ToastKind.Success)
            }

            div(fomantic.content).new() {
                div(fomantic.ui.secondary.pointing.menu).new{
                    a(fomantic.ui.item.active, "/settings/users").text("Benutzer")
                    a(fomantic.ui.item, "/settings/keys").text("Schlüssel")
                }

                button(fomantic.ui.button).text("Neuer Benutzer").on.click {
                    modal.open()
                }

                div(fomantic.ui.divider.hidden)

                render(users) { rUsers ->

                    logger.info("List of users did change")

                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().text("Vorname")
                                th().text("Nachname")
                                th().text("E-Mail")
                                th().text("Erstellt am")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rUsers.forEach { user ->

                                tr().new {
                                    td().text(user.firstName ?: "")
                                    td().text(user.lastName ?: "")
                                    td().text(user.emailAdress ?: "")
                                    td().text(user.created?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "")
                                    td().new {

                                        i(fomantic.ui.edit.icon).on.click {
                                            logger.info("Editing user ${user.firstName} ${user.lastName}")

                                            val editModal = editUserModal(user){ user, action ->
                                                when(action){
                                                    UserEditAction.UserDeleted -> {
                                                        users.value = users.value.filter { it._id!=user._id }
                                                        pageArea.showToast("Benutzer entfernt", ToastKind.Success)
                                                    }
                                                    UserEditAction.PasswordChanged -> {
                                                        pageArea.showToast("Passwort geändert", ToastKind.Success)
                                                    }
                                                    UserEditAction.UserModified -> {
                                                        users.value = users.value.map{
                                                            if(it._id==user._id) user else it
                                                        }
                                                        pageArea.showToast("Benutzer bearbeitet", ToastKind.Success)
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