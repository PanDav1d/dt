package de.doctag.keysrv.ui.modals

import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.User
import de.doctag.keysrv.ui.forms.userAddForm
import de.doctag.keysrv.ui.modal
import kweb.ElementCreator
import kweb.logger
import java.time.ZonedDateTime

fun ElementCreator<*>.addUserModal(onUserAdd: (u:User)->Unit) = modal("Benutzer hinzufügen"){ modal->
    val user  = User()
    userAddForm(user) { userObj, pass ->
        logger.info("Creating user with e-mail ${userObj.emailAdress} and password ${pass}")
        userObj.created = ZonedDateTime.now()

        userObj.apply {
            DbContext.users.insertOne(userObj)
        }

        onUserAdd(userObj)

        modal.close()
    }
}