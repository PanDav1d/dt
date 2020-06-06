package de.doctag.docsrv.ui.forms

import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.User
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import org.litote.kmongo.findOne
import org.litote.kmongo.regex


fun ElementCreator<*>.userAddForm(userObj: User, onSaveClick: (user:User, password: String)->Unit){
    val user = KVar(userObj)
    val password = KVar("")
    val passwordConfirm = KVar("")

    formControl { formCtrl ->

        formCtrl.withValidation {
            if(password.value != passwordConfirm.value){
                "Beide Passwort-Felder müssen übereinstimmen"
            }
            else {
                null
            }
        }

        div(fomantic.ui.field).new{
            label().text("Name")
            div(fomantic.ui.two.fields).new {
                formInput(null, "Vorname", true, user.propertyOrDefault(User::firstName, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage("Bitte geben Sie Ihren Vornamen an.")

                formInput(null, "Nachname", true, user.propertyOrDefault(User::lastName, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage("Bitte geben Sie Ihren Nachnamen an.")
            }
        }

        formInput( "E-Mail", "E-Mail", false, user.propertyOrDefault(User::emailAdress, ""))
            .with(formCtrl)
            .validate {
                when{
                    it?.matches("^[A-Za-z0-9+_.-]+@(.+)$".toRegex()) != true -> "Bitte geben Sie eine gültige E-Mail Addresse an"
                    DbContext.users.findOne(User::emailAdress.regex(it, "i") ) != null -> "Die E-Mail Addresse ist bereits vergeben"
                    else -> null
                }
            }

        formInput( "Passwort", "Passwort", true, password, InputType.password)
            .with(formCtrl)

        formInput( "Passwort bestätigen", "Passwort bestätigen", true, passwordConfirm, InputType.password)
            .with(formCtrl)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            onSaveClick(user.value, password.value)
        }
    }
}

fun ElementCreator<*>.userEditForm(userObj: User, onSaveClick: (user:User)->Unit){
    val user = KVar(userObj)

    formControl { formCtrl ->

        formInput("Vorname", "Vorname", true, user.propertyOrDefault(User::firstName, ""))
            .with(formCtrl)
            .withInputMissingErrorMessage("Bitte geben Sie Ihren Vornamen an.")

        formInput("Nachname", "Nachname", true, user.propertyOrDefault(User::lastName, ""))
            .with(formCtrl)
            .withInputMissingErrorMessage("Bitte geben Sie Ihren Nachnamen an.")


        formInput( "E-Mail", "E-Mail", false, user.propertyOrDefault(User::emailAdress, ""))
            .with(formCtrl)
            .validate {
                when{
                    it?.matches("^[A-Za-z0-9+_.-]+@(.+)$".toRegex()) != true -> "Bitte geben Sie eine gültige E-Mail Addresse an"
                    DbContext.users.findOne(User::emailAdress.regex(it, "i") ) != null && it != userObj.emailAdress -> "Die E-Mail Addresse ist bereits vergeben"
                    else -> null
                }
            }


        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            onSaveClick(user.value)
        }
    }
}

fun ElementCreator<*>.userPasswordEditForm(userObj: User, onSaveClick: (password: String)->Unit){
    val user = KVar(userObj)
    val password = KVar("")
    val passwordConfirm = KVar("")

    formControl { formCtrl ->

        formCtrl.withValidation {
            if(password.value != passwordConfirm.value){
                "Beide Passwort-Felder müssen übereinstimmen"
            }
            else {
                null
            }
        }


        formInput( "Neues Passwort", "Passwort", true, password, InputType.password)
            .with(formCtrl)

        formInput( "Neues Passwort bestätigen", "Passwort bestätigen", true, passwordConfirm, InputType.password)
            .with(formCtrl)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            onSaveClick(password.value)
        }
    }
}

fun ElementCreator<*>.userDeleteForm(userObj: User, onSaveClick: ()->Unit){

    val emailConfirm = KVar("")

    formControl { formCtrl ->

        formCtrl.withValidation {
            if(userObj.emailAdress != emailConfirm.value){
                "Beide Felder müssen übereinstimmen"
            }
            else {
                null
            }
        }

        h2().text("Den Benutzer ${userObj.firstName} ${userObj.lastName} wirklich löschen?")
        p().text("Bitte geben Sie die E-Mail Addresse ${userObj.emailAdress} des Nutzers ein um die Löschung zu bestätigen.")

        formInput( "E-Mail Addresse", "E-Mail", true, emailConfirm, InputType.text)
            .with(formCtrl)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl, "Löschen", fomantic.ui.button.red){
            onSaveClick()
        }
    }
}