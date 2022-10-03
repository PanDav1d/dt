package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.generatePasswordHash
import de.doctag.docsrv.i18n
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.User
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.TabPane
import de.doctag.docsrv.ui.forms.*
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.tab
import kweb.ElementCreator
import kweb.logger
import org.litote.kmongo.eq
import org.litote.kmongo.replaceOneById

enum class UserEditAction{
    UserModified,
    PasswordChanged,
    UserDeleted
}

fun ElementCreator<*>.editUserModal(user: User, onEdit: (u:User, action: UserEditAction)->Unit) =
        modal(i18n("ui.modals.editUserModal.title","${user.firstName} ${user.lastName} bearbeiten")) { modal ->

            tab(TabPane(i18n("ui.modals.editUserModal.profileTitle","Profil")) {
                    userEditForm(user) { user ->
                        logger.info("Saving changed user")
                        db().users.replaceOneById(user._id!!, user)

                        onEdit(user, UserEditAction.UserModified)
                    }
                },
                TabPane(i18n("ui.modals.editUserModal.changePasswordTitle","Passwort ändern")) {
                    userPasswordEditForm(user) { newPassword ->
                        user.passwordHash = generatePasswordHash(newPassword)
                        db().users.replaceOneById(user._id!!, user)

                        onEdit(user, UserEditAction.PasswordChanged)
                    }
                },
                TabPane(i18n("ui.modals.editUserModal.loginsTitle","Anmeldungen")){
                    userSessionsForm(user){
                        db().users.replaceOneById(user._id!!, user)
                        onEdit(user, UserEditAction.UserModified)
                    }
                },
                TabPane(i18n("ui.modals.editUserModal.doctagAppTitle","Doctag App")){
                    userAppForm(user){
                        db().users.replaceOneById(user._id!!, user)
                        onEdit(user, UserEditAction.UserModified)
                    }
                },
                TabPane(i18n("ui.modals.editUserModal.deleteTitle","Löschen")) {
                    userDeleteForm(user){
                        logger.info("User ${user.emailAdress} will be removed")

                        db().users.deleteOne(User::_id eq user._id)
                        onEdit(user, UserEditAction.UserDeleted)

                        modal.close()
                    }
                })
        }