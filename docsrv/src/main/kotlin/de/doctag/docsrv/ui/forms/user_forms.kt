package de.doctag.docsrv.ui.forms

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.getQRCodeImageAsDataUrl
import de.doctag.docsrv.model.*
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import kweb.util.toJson
import org.litote.kmongo.findOne
import org.litote.kmongo.regex
import java.time.ZonedDateTime
import java.util.*


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
                    db().users.findOne(User::emailAdress.regex(it, "i") ) != null -> "Die E-Mail Addresse ist bereits vergeben"
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
                    db().users.findOne(User::emailAdress.regex(it, "i") ) != null && it != userObj.emailAdress -> "Die E-Mail Addresse ist bereits vergeben"
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

fun ElementCreator<*>.userSessionsForm(userObj: User, onSaveClick: () -> Unit){

    val changeCounter = KVar(0)
    render(changeCounter){
        table(fomantic.ui.celled.table).new {
            thead().new {
                tr().new {
                    th().text("Auf Gerät")
                    th().text("Gültig bis")
                    th().text("")
                }
            }
            tbody().new{
                userObj.sessions?.forEach { session->
                    tr().new {
                        td().text(session.name ?: "")
                        td().text(session.expires.formatDateTime())
                        td().new{
                            i(fomantic.ui.remove.icon).on.click {
                                userObj.sessions = userObj.sessions?.filter { it != session }
                                changeCounter.value += 1
                            }
                        }
                    }
                }
                if(userObj.sessions.isNullOrEmpty()){
                    tr().new {
                        td(mapOf("colspan" to "3")).text("Keine Anmeldungen vorhanden")
                    }
                }
            }
        }

        if(changeCounter.value > 0) {
            buttonWithLoader("Änderungen übernehmen", fomantic.ui.button.primary) {
                onSaveClick()
                changeCounter.value = 0
            }
        }
    }
}

data class SmartphoneLoginData(val doctagUrl:String, val sessionId: String)

fun ElementCreator<*>.userAppForm(userObj:User, onSaveClick: () -> Unit) = useState(null as Session?){ newSession, setState ->

    if(newSession == null) {
        button(fomantic.ui.button.primary).text("Hinzufügen").on.click {
            val session = Session(UUID.randomUUID().toString(), ZonedDateTime.now().plusYears(5), "Doctag App Anmeldung")
            userObj.sessions = (userObj.sessions ?: listOf()).plus(session)

            onSaveClick()
            setState(session)
        }
    } else {

        val qr = getQRCodeImageAsDataUrl(SmartphoneLoginData(db().currentConfig.hostname, newSession.sessionId).toJson(), 400,400, 5)

        h3(fomantic.ui.header).new {
            span().text("Doctag App Anmeldung")
            div(fomantic.ui.sub.header).text("Direkt scannen")
        }

        img(src=qr)

        db().currentConfig.outboundMail?.let { mailConfig->
            userObj.emailAdress?.let{ mailAddress->

                useState(false){didSendMail, markMailAsSent->
                    if(!didSendMail) {

                        buttonWithLoader("Per E-Mail senden") {
                            sendAppLoginMail(mailConfig, mailAddress, qr)
                            markMailAsSent(true)
                        }
                    }
                    else {
                        div(fomantic.ui.message.info).new {
                            div(fomantic.header).text("Mail gesendet")
                            p().text("Die Zugangsdaten wurden erfolgreich an ${mailAddress} gesendet.")
                        }
                    }
                }
            }
        }
    }
}

private fun sendAppLoginMail(conf: OutboundMailConfig, toAddress: String, qrCode: String) : Boolean{
    val email = EmailContent(
        greeting = "Hallo",
        text = """
                    | Anbei erhalten Sie Ihre Zugangsdaten für die Doctag App. 
                    | Bitte öffnen Sie die Doctag App und scannen den 
                    | nachfolgenden QR-Code um sich anzumelden.
                    | 
                    | <img src="${qrCode}"/>
                    """.trimMargin(),
        actionText = null,
        actionUrl = null,
        byeText = "Viele Grüße "
    )

    val ms = MailSender(
        receiverAddresses = listOf(toAddress),
        fromAddress = conf.fromAddress,
        smtpHost = conf.server!!,
        smtpUser = conf.user,
        smtpPassword = conf.password,
        subject = "Zugangsdaten für die Doctag App",
        content = email
    )
    return ms.send()
}