package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.*
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.remotes.MailReceiver
import de.doctag.docsrv.ui.*
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender
import de.doctag.lib.model.Address
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

fun ElementCreator<*>.mailSettingsEditForm(outbound: OutboundMailConfig, inbound: InboundMailConfig, onSaveClick: (outbound: OutboundMailConfig, inbound: InboundMailConfig)->Unit){
    val verboseName = KVar("")
    val address = KVar(Address())
    val outboundConfig = KVar(outbound)
    val inboundConfig = KVar(inbound)

    formControl { formCtrl ->

        h4(fomantic.ui.dividing.header).text("Mailversand")

        formInput("SMTP Server", "smtp.test.de", false, outboundConfig.propertyOrDefault(OutboundMailConfig::server, ""))
            .with(formCtrl)



        formInput("Benutzername", "max_mueller", false, outboundConfig.propertyOrDefault(OutboundMailConfig::user, ""))
                .with(formCtrl)

        formInput("Passwort", "", false, outboundConfig.propertyOrDefault(OutboundMailConfig::password, ""), inputType = InputType.password)
                .with(formCtrl)

        formInput("Absender", "", false, outboundConfig.propertyOrDefault(OutboundMailConfig::fromAddress, ""))
                .with(formCtrl)


        h4(fomantic.ui.dividing.header).text("Mail Empfang")

        checkBoxInput(
            "Dokumenten-Empfang per Mail aktiv",
            inboundConfig.propertyOrDefault(InboundMailConfig::shouldReceiveDocumentsViaMail, false)
        )

        val protocol = KVar(inboundConfig.value.protocol.toString())
        protocol.addListener { oldVal, newVal ->
            inboundConfig.value.protocol = InboundMailProtocol.valueOf(newVal.toUpperCase())
        }
        radioInput("Protokoll", mapOf("IMAP" to InboundMailProtocol.IMAP.name, "POP3" to InboundMailProtocol.POP3.name), false, true,  protocol)


        formInput("IMAP / POP Server", "smtp.test.de", false, inboundConfig.propertyOrDefault(InboundMailConfig::server, ""))
                .with(formCtrl)



        formInput("Benutzername", "max_mueller", false, inboundConfig.propertyOrDefault(InboundMailConfig::user, ""))
                    .with(formCtrl)




        formInput("Passwort", "", false, inboundConfig.propertyOrDefault(InboundMailConfig::password, ""), inputType = InputType.password)
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

        button(fomantic.ui.button.tertiary.blue).text("Test-Mail senden").on.click {
            logger.info ("Sending test mail")

            GlobalScope.launch {
                if(sendTestMail(outboundConfig.value, browser.authenticatedUser!!.emailAdress!!)) {
                    testResult.value = UserMessage(DisplayMessageKind.Success, "Erfolg", "E-Mail erfolgreich versendet")
                } else {
                    testResult.value = UserMessage(DisplayMessageKind.Error, "Fehlgeschlagen", "E-Mail konnte nicht versendet werden")
                }
            }
        }

        button(fomantic.ui.button.tertiary.blue.disabled(inbound.protocol == null || inbound.server == null || inbound.user == null || inbound.password == null)).text("Abruf testen").on.click {
            logger.info ("Test receiving mail")
            val recv = MailReceiver.connect(inbound.protocol!!, inbound.server!!, inbound.user!!, inbound.password!!)
            val messages = recv?.receive()

            if(messages != null) {
                testResult.value = UserMessage(DisplayMessageKind.Success, "Erfolg", "E-Mail Abruf erfolgreich. ${messages.size} Nachrichten empfangen")
            }
            else {
                testResult.value = UserMessage(DisplayMessageKind.Error, "Fehler", "E-Mail Abruf fehlgeschlagen. Bitte prüfen Sie die Einstellungen")
            }
        }
    }
}

private fun sendTestMail(conf: OutboundMailConfig, toAddress: String) : Boolean{
    val email = EmailContent(
            greeting = "Hallo",
            text = """
                    | Dies ist eine Test-Nachricht welche über die Web-Konsole versendet wurde
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
            subject = "Test-Mail",
            content = email
    )
    return ms.send()
}
