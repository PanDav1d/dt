package de.docward.keysrv.ui.modals

import de.docward.keysrv.model.DbContext
import de.docward.keysrv.model.User
import de.docward.keysrv.ui.forms.userAddForm
import de.docward.keysrv.ui.modal
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