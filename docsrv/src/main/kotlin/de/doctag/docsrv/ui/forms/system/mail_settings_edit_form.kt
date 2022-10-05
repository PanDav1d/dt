package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.remotes.MailReceiver
import de.doctag.docsrv.ui.*
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender
import de.doctag.lib.SendMailProtocol
import de.doctag.lib.model.Address
import doctag.translation.I18n
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.property
import kweb.state.render
import org.litote.kmongo.findOne
import org.litote.kmongo.regex
import java.util.*

fun ElementCreator<*>.mailSettingsEditForm(outbound: OutboundMailConfig, inbound: InboundMailConfig, onSaveClick: (outbound: OutboundMailConfig, inbound: InboundMailConfig)->Unit){
    val verboseName = KVar("")
    val address = KVar(Address())
    val outboundConfig = KVar(outbound)
    val inboundConfig = KVar(inbound)

    formControl { formCtrl ->

        h4(fomantic.ui.dividing.header).i18nText("ui.forms.system.mailSettingsEditForm.sendMailTitle","Mailversand")

        val smtp_protocol = KVar(inboundConfig.value.protocol.toString())
        smtp_protocol.addListener { oldVal, newVal ->
            outboundConfig.value.protocol = SendMailProtocol.valueOf(newVal.toUpperCase())
        }

        formInput(i18n("ui.forms.system.mailSettingsEditForm.sendMailRemoteHost","SMTP Server"), "smtp.test.de", false, outboundConfig.propertyOrDefault(OutboundMailConfig::server, ""))
            .with(formCtrl)

        radioInput(i18n("ui.forms.system.mailSettingsEditForm.sendMailProtocol","Protokoll"), mapOf("SMTP" to SendMailProtocol.SMTP.name, "SMTPS" to SendMailProtocol.SMTPS.name), false, true,  smtp_protocol)

        formInput(i18n("ui.forms.system.mailSettingsEditForm.sendMailUsername","Benutzername"), "max_mueller", false, outboundConfig.propertyOrDefault(OutboundMailConfig::user, ""))
                .with(formCtrl)

        formInput(i18n("ui.forms.system.mailSettingsEditForm.sendMailPassword","Passwort"), "", false, outboundConfig.propertyOrDefault(OutboundMailConfig::password, ""), inputType = InputType.password)
                .with(formCtrl)

        formInput(i18n("ui.forms.system.mailSettingsEditForm.sendMailFromAddress","Absender"), "", false, outboundConfig.propertyOrDefault(OutboundMailConfig::fromAddress, ""))
                .with(formCtrl)


        h4(fomantic.ui.dividing.header).i18nText("ui.forms.system.mailSettingsEditForm.fetchMailHeader","Mail Empfang")

        checkBoxInput(
            i18n("ui.forms.system.mailSettingsEditForm.fetchMailActive","Dokumenten-Empfang per Mail aktiv"),
            inboundConfig.propertyOrDefault(InboundMailConfig::shouldReceiveDocumentsViaMail, false)
        )

        val protocol = KVar(inboundConfig.value.protocol.toString())
        protocol.addListener { oldVal, newVal ->
            inboundConfig.value.protocol = InboundMailProtocol.valueOf(newVal.toUpperCase())
        }
        radioInput(i18n("ui.forms.system.mailSettingsEditForm.fetchMailProtocol","Protokoll"), mapOf("IMAP" to InboundMailProtocol.IMAP.name, "POP3" to InboundMailProtocol.POP3.name), false, true,  protocol)


        formInput(i18n("ui.forms.system.mailSettingsEditForm.fetchMailRemoteHost","IMAP / POP Server"), "smtp.test.de", false, inboundConfig.propertyOrDefault(InboundMailConfig::server, ""))
                .with(formCtrl)



        formInput(i18n("ui.forms.system.mailSettingsEditForm.fetchMailUsername","Benutzername"), "max_mueller", false, inboundConfig.propertyOrDefault(InboundMailConfig::user, ""))
                    .with(formCtrl)




        formInput(i18n("ui.forms.system.mailSettingsEditForm.fetchMailPassword","Passwort"), "", false, inboundConfig.propertyOrDefault(InboundMailConfig::password, ""), inputType = InputType.password)
                .with(formCtrl)



        displayErrorMessages(formCtrl)

        val testResult = KVar<UserMessage?>(null)

        render(testResult){ userMessage->
            userMessage?.let {
                displayMessage(it)
                GlobalScope.launch {
                    delay(5000)
                    testResult.value = null
                }
            }
        }

        formSubmitButton(formCtrl) {
            onSaveClick(outboundConfig.value, inboundConfig.value)
        }

        button(fomantic.ui.button.tertiary.blue).i18nText("ui.forms.system.mailSettingsEditForm.sendMailTest.buttonText","Test-Mail senden").on.click {
            logger.info ("Sending test mail")

            GlobalScope.launch {
                if(sendTestMail(outboundConfig.value, browser.authenticatedUser!!.emailAdress!!, locale = browser.sessionLanguage)) {
                    testResult.value = UserMessage(DisplayMessageKind.Success, i18n("ui.forms.system.mailSettingsEditForm.sendMailTest.successTitle","Erfolg"), i18n("ui.forms.system.mailSettingsEditForm.sendMailTest.successDescription","E-Mail erfolgreich versendet"))
                } else {
                    testResult.value = UserMessage(DisplayMessageKind.Error, i18n("ui.forms.system.mailSettingsEditForm.sendMailTest.errorTitle","Fehlgeschlagen"), i18n("ui.forms.system.mailSettingsEditForm.sendMailTest.errorDescription","E-Mail konnte nicht versendet werden"))
                }
            }
        }

        button(fomantic.ui.button.tertiary.blue.disabled(inbound.protocol == null || inbound.server == null || inbound.user == null || inbound.password == null)).i18nText("ui.forms.system.mailSettingsEditForm.fetchMailTest.button","Abruf testen").on.click {
            logger.info ("Test receiving mail")
            val recv = MailReceiver.connect(inbound.protocol!!, inbound.server!!, inbound.user!!, inbound.password!!)
            val messages = recv?.receive()

            if(messages != null) {
                testResult.value = UserMessage(DisplayMessageKind.Success, i18n("ui.forms.system.mailSettingsEditForm.fetchMailSuccess.title","Erfolg"), i18n("ui.forms.system.mailSettingsEditForm.fetchMailSuccess.description","E-Mail Abruf erfolgreich. ${messages.size} Nachrichten empfangen"))
            }
            else {
                testResult.value = UserMessage(DisplayMessageKind.Error, i18n("ui.forms.system.mailSettingsEditForm.fetchMailFailedErrorTitle","Fehler"), i18n("ui.forms.system.mailSettingsEditForm.fetchMailFailedErrorDescription", "E-Mail Abruf fehlgeschlagen. Bitte prüfen Sie die Einstellungen"))
            }
        }
    }
}

private fun sendTestMail(conf: OutboundMailConfig, toAddress: String, locale: Locale) : Boolean{
    val email = EmailContent(
            greeting = I18n.t("ui.forms.system.mailSettingsEditForm.testMail.greeting","Hallo", language = locale),
            text = I18n.t("ui.forms.system.mailSettingsEditForm.testMail.text", """Dies ist eine Test-Nachricht welche über die Web-Konsole versendet wurde""", language = locale),
            actionText = null,
            actionUrl = null,
            byeText = I18n.t("ui.forms.system.mailSettingsEditForm.testMail.byeText","Viele Grüße ", language = locale)
    )

    val ms = MailSender(
            receiverAddresses = listOf(toAddress),
            fromAddress = conf.fromAddress,
            smtpHost = conf.server!!,
            smtpUser = conf.user,
            smtpPassword = conf.password,
            subject = I18n.t("ui.forms.system.mailSettingsEditForm.testMail.subject","Test-Mail",language=locale),
            content = email,
            smtpProtocol = conf.protocol
    )
    return ms.send()
}