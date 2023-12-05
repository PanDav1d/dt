package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.generatePasswordHash
import de.doctag.docsrv.i18n
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.User
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.forms.userAddForm
import de.doctag.docsrv.ui.modal
import kweb.ElementCreator
import kweb.logger
import java.time.ZonedDateTime

fun ElementCreator<*>.addUserModal(onUserAdd: (u:User)->Unit) = modal(i18n("ui.modals.addUserModal.title","Benutzer hinzufügen")){ modal->
    val user  = User(isAdmin = false)
    userAddForm(user) { userObj, pass ->
        logger.info("Creating user with e-mail ${userObj.emailAdress} and password ${pass}")
        userObj.created = ZonedDateTime.now()
        userObj.passwordHash = generatePasswordHash(pass)

        userObj.apply {
            db().users.insertOne(userObj)
        }

        onUserAdd(userObj)

        modal.close()
    }
}