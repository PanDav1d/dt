package de.docward.docsrv.ui.modals

import de.docward.docsrv.generatePasswordHash
import de.docward.docsrv.model.DbContext
import de.docward.docsrv.model.User
import de.docward.docsrv.ui.TabPane
import de.docward.docsrv.ui.forms.userDeleteForm
import de.docward.docsrv.ui.forms.userEditForm
import de.docward.docsrv.ui.forms.userPasswordEditForm
import de.docward.docsrv.ui.modal
import de.docward.docsrv.ui.tab
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
        modal("${user.firstName} ${user.lastName} bearbeiten") { modal ->
            tab(TabPane("Profil") {
                userEditForm(user) { user ->
                    logger.info("Saving changed user")
                    DbContext.users.replaceOneById(user._id!!, user)

                    onEdit(user, UserEditAction.UserModified)
                }
            },
            TabPane("Passwort ändern") {
                userPasswordEditForm(user) { newPassword ->
                    user.passwordHash = generatePasswordHash(newPassword)
                    DbContext.users.replaceOneById(user._id!!, user)

                    onEdit(user, UserEditAction.PasswordChanged)

                }
            },
            TabPane("Löschen") {
                userDeleteForm(user){
                    logger.info("User ${user.emailAdress} will be removed")

                    DbContext.users.deleteOne(User::_id eq user._id)
                    onEdit(user, UserEditAction.UserDeleted)

                    modal.close()
                }
            })
        }