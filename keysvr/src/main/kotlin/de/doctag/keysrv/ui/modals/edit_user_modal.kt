package de.doctag.keysrv.ui.modals

import de.doctag.keysrv.generatePasswordHash
import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.User
import de.doctag.keysrv.ui.TabPane
import de.doctag.keysrv.ui.forms.userDeleteForm
import de.doctag.keysrv.ui.forms.userEditForm
import de.doctag.keysrv.ui.forms.userPasswordEditForm
import de.doctag.keysrv.ui.modal
import de.doctag.keysrv.ui.tab
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