package de.doctag.docsrv.ui.forms

import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.docsrv.pdf_builder.LoginLinkDocument
import de.doctag.docsrv.ui.*
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender
import doctag.translation.I18n
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
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
                i18n("ui.forms.userForms.userAddForm.passwordDoNotMatchError","Beide Passwort-Felder müssen übereinstimmen")
            }
            else {
                null
            }
        }

        div(fomantic.ui.field).new{
            label().i18nText("ui.forms.userForms.userAddForm.nameLabel","Name")
            div(fomantic.ui.two.fields).new {
                formInput(null, i18n("ui.forms.userForms.userAddForm.firstNamePlaceholder","Vorname"), true, user.propertyOrDefault(User::firstName, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage(i18n("ui.forms.userForms.userAddForm.provideFirstNameError","Bitte geben Sie Ihren Vornamen an."))

                formInput(null, i18n("ui.forms.userForms.userAddForm.lastNamePlaceholder","Nachname"), true, user.propertyOrDefault(User::lastName, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage(i18n("ui.forms.userForms.userAddForm.provideLastNameError","Bitte geben Sie Ihren Nachnamen an."))
            }
        }

        formInput( i18n("ui.forms.userForms.userAddForm.emailLabel","E-Mail"), i18n("ui.forms.userForms.userAddForm.emailPlaceholder","E-Mail"), false, user.propertyOrDefault(User::emailAdress, ""))
            .with(formCtrl)
            .validate {
                when{
                    it?.matches("^[A-Za-z0-9+_.-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$".toRegex(RegexOption.IGNORE_CASE)) != true -> i18n("ui.forms.userForms.userAddForm.emailInvalidErrorMessage","Bitte geben Sie eine gültige E-Mail Addresse an")
                    db().users.findOne(User::emailAdress.regex(it, "i") ) != null -> i18n("ui.forms.userForms.userAddForm.emailAddressTakenErrorMessage","Die E-Mail Addresse ist bereits vergeben")
                    else -> null
                }
            }

        formInput( i18n("ui.forms.userForms.userAddForm.passwordLabel","Passwort"), i18n("ui.forms.userForms.userAddForm.passwordPlaceholder","Passwort"), true, password, InputType.password)
            .with(formCtrl)

        formInput( i18n("ui.forms.userForms.userAddForm.passwordConfirmLabel","Passwort bestätigen"), i18n("ui.forms.userForms.userAddForm.passwordConfirmPlaceholder","Passwort bestätigen"), true, passwordConfirm, InputType.password)
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

        formInput(i18n("ui.forms.userForms.userEditForm.firstNameLabel","Vorname"), i18n("ui.forms.userForms.userEditForm.firstNamePlaceholder","Vorname"), true, user.propertyOrDefault(User::firstName, ""))
            .with(formCtrl)
            .withInputMissingErrorMessage(i18n("ui.forms.userForms.userEditForm.provideFirstnameErrorMessage","Bitte geben Sie Ihren Vornamen an."))

        formInput(i18n("ui.forms.userForms.userEditForm.lastNameLabel","Nachname"), i18n("ui.forms.userForms.userEditForm.lastNamePlaceholder","Nachname"), true, user.propertyOrDefault(User::lastName, ""))
            .with(formCtrl)
            .withInputMissingErrorMessage(i18n("ui.forms.userForms.userEditForm.provideLastNameError","Bitte geben Sie Ihren Nachnamen an."))


        formInput( i18n("ui.forms.userForms.userEditForm.emailLabel","E-Mail"), i18n("ui.forms.userForms.userEditForm.emailPlaceholder","E-Mail"), false, user.propertyOrDefault(User::emailAdress, ""))
            .with(formCtrl)
            .validate {
                when{
                    it?.matches("^[A-Za-z0-9+_.-]+@(.+)$".toRegex()) != true -> i18n("ui.forms.userForms.userEditForm.provideEmailerrorMessage","Bitte geben Sie eine gültige E-Mail Addresse an")
                    db().users.findOne(User::emailAdress.regex(it, "i") ) != null && it != userObj.emailAdress -> i18n("ui.forms.userForms.userEditForm.emailAddressAlreadyTakenErrorMessage","Die E-Mail Addresse ist bereits vergeben")
                    else -> null
                }
            }

        checkBoxInput(i18n("ui.forms.userForms.userEditForm.isAdmin","Ist Administrator?"), user.propertyOrDefault(User::isAdmin, false))


        formCtrl.withValidation {
            if(user.value._id == browser.authenticatedUser?._id){
                if(user.value.isAdmin == false && browser.authenticatedUser?.isAdmin==true){
                    i18n("ui.forms.userForms.userEditForm.validationError.userCantDropAdminRightsHimself","Sie dürfen sich nicht selbst die Admin-Rechte entziehen.")
                } else {
                    null
                }
            } else {
                null
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
                i18n("ui.forms.userForms.userPasswordEditForm.passwordsDoNotMatchError","Beide Passwort-Felder müssen übereinstimmen")
            }
            else {
                null
            }
        }


        formInput( i18n("ui.forms.userForms.userPasswordEditForm.newPasswordLabel","Neues Passwort"), i18n("ui.forms.userForms.userPasswordEditForm.newPasswordPlaceholder","Passwort"), true, password, InputType.password)
            .with(formCtrl)

        formInput( i18n("ui.forms.userForms.userPasswordEditForm.confirmPasswordLabel","Neues Passwort bestätigen"), i18n("ui.forms.userForms.userPasswordEditForm.confirmPasswordPlaceholder","Passwort bestätigen"), true, passwordConfirm, InputType.password)
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
                i18n("ui.forms.userForms.userDeleteForm.usernamesDoNotMatchError","Beide Felder müssen übereinstimmen")
            }
            else {
                null
            }
        }

        h2().i18nText("ui.forms.userForms.userDeleteForm.deleteConfirmMessage","Den Benutzer ${userObj.firstName} ${userObj.lastName} wirklich löschen?")
        p().i18nText("ui.forms.userForms.userDeleteForm.pleaseTypeEmailMessage","Bitte geben Sie die E-Mail Addresse ${userObj.emailAdress} des Nutzers ein um die Löschung zu bestätigen.")

        formInput( i18n("ui.forms.userForms.userDeleteForm.emailLabel","E-Mail Addresse"), i18n("ui.forms.userForms.userDeleteForm.emailPlaceholder","E-Mail"), true, emailConfirm, InputType.text)
            .with(formCtrl)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl, i18n("ui.forms.userForms.userDeleteForm.deleteButton","Löschen"), fomantic.ui.button.red){
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
                    th().i18nText("ui.forms.userForms.userSessionForm.onDevice","Auf Gerät")
                    th().i18nText("ui.forms.userForms.userSessionForm.validTill","Gültig bis")
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
                        td(mapOf("colspan" to "3")).i18nText("ui.forms.userForms.userSessionForm.noLoginAvailableMessage","Keine Anmeldungen vorhanden")
                    }
                }
            }
        }

        if(changeCounter.value > 0) {
            buttonWithLoader(i18n("ui.forms.userForms.userSessionForm.confirmChangesButton","Änderungen übernehmen"), fomantic.ui.button.primary) {
                onSaveClick()
                changeCounter.value = 0
            }
        }
    }
}

data class SmartphoneLoginData(val doctagUrl:String, val sessionId: String)

fun ElementCreator<*>.userLoginLinkForm(userObj:User, onSaveClick: () -> Unit) = useState(null as Session?){ newSession, setState ->

    if(newSession == null) {
        button(fomantic.ui.button.primary).i18nText("ui.forms.userForms.userAppForm.addButton","Hinzufügen").on.click {
            val session = Session(UUID.randomUUID().toString(), ZonedDateTime.now().plusYears(5), i18n("ui.forms.userForms.userAppForm.appSessionName","DocTag Anmeldelink"))
            userObj.sessions = (userObj.sessions ?: listOf()).plus(session)

            onSaveClick()
            setState(session)
        }
    } else {

        val loginUrl = "https://${db().currentConfig.hostname}/login/?sessionId=${newSession.sessionId}"
        val qr = getQRCodeImageAsDataUrl(loginUrl, 400,400, 5)

        h3(fomantic.ui.header).new {
            span().i18nText("ui.forms.userForms.userAppForm.addSessionForAppHeader","Anmeldelink")
            div(fomantic.ui.sub.header).i18nText("ui.forms.userForms.userAppForm.scanImmediately","Direkt scannen")
        }

        img(src=qr)

        db().currentConfig.outboundMail?.let { mailConfig->
            userObj.emailAdress?.let{ mailAddress->

                useState(false){didSendMail, markMailAsSent->
                    if(!didSendMail) {

                        buttonWithLoader(i18n("ui.forms.userForms.userAppForm.sendViaEmail","Per E-Mail senden")) {
                            sendAppLoginMail(mailConfig, mailAddress, qr, browser.sessionLanguage)
                            markMailAsSent(true)
                        }
                    }
                    else {
                        div(fomantic.ui.message.info).new {
                            div(fomantic.header).i18nText("ui.forms.userForms.userAppForm.successMessageTitle","Mail gesendet")
                            p().i18nText("ui.forms.userForms.userAppForm.successMessageBody","Die Zugangsdaten wurden erfolgreich an ${mailAddress} gesendet.")
                        }
                    }
                }
            }
        }

        val keyCard = LoginLinkDocument(userObj, newSession,db(), browser.sessionLanguage)
        br()
        a(href="data:application/pdf;base64,${keyCard.asBase64Str()}", attributes = fomantic.ui.button.withAttribute("download", "keycard_${userObj.lastName?.toLowerCase()}_${userObj.firstName?.toLowerCase()}.pdf")).apply {
            text.value = this@userLoginLinkForm.i18n("ui.forms.userForms.userAppForm.downloadKeycard", "Herunterladen")
        }

    }
}

private fun sendAppLoginMail(conf: OutboundMailConfig, toAddress: String, qrCode: String, locale: Locale) : Boolean{
    val email = EmailContent(
        greeting = I18n.t("ui.forms.userForms.sendAppLoginMail.greeting","Hallo", language = locale),
        text = I18n.t("ui.forms.userForms.sendAppLoginMail.mailBody","""
                    Anbei erhalten Sie Ihre Zugangsdaten für die Doctag App. 
                    Bitte öffnen Sie die Doctag App und scannen den 
                    nachfolgenden QR-Code um sich anzumelden.
                    """, language = locale) + "<img src=\"${qrCode}\"/>",
        actionText = null,
        actionUrl = null,
        byeText = I18n.t("ui.forms.userForms.sendAppLoginMail.byteText","Viele Grüße ", language = locale)
    )

    val ms = MailSender(
        receiverAddresses = listOf(toAddress),
        fromAddress = conf.fromAddress,
        smtpHost = conf.server!!,
        smtpUser = conf.user,
        smtpPassword = conf.password,
        subject = I18n.t("ui.forms.userForms.sendAppLoginMail.subject","Zugangsdaten für die Doctag App", language = locale),
        content = email,
        smtpProtocol = conf.protocol
    )
    return ms.send()
}