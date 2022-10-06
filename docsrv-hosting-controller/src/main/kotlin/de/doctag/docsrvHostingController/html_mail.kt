package de.doctag.docsrvHostingController


import de.doctag.docsrv.model.User
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender
import de.doctag.lib.SendMailProtocol
import java.time.ZonedDateTime


fun sendServerPinMail(receiver: User, serverpin: String){
    val email = EmailContent(
            greeting = "Hallo ${receiver.firstName} ${receiver.lastName}",
            text = """Die für die Einrichtung Ihres Doc-Servers notwendige Server-Pin lautet: <strong>${serverpin}</strong> Bitte geben Sie diesen Server-Pin auf der Setup-Seite ein, um die Einrichtung fortzusetzen.""".trimMargin(),
            actionText = null,
            actionUrl = null,
            byeText = "Viele Grüße "
    )

    val mail = MailSender(
            receiverAddresses = mutableListOf(receiver.emailAdress!!),
            subject = "Einrichtung Ihrer Docsrv Instanz",
            content = email,
            smtpHost = Config.instance.smtpServer,
            smtpUser = Config.instance.smtpUser,
            smtpPassword = Config.instance.smtpPassword,
            fromAddress = Config.instance.fromAddress,
            ZonedDateTime.now(),
            SendMailProtocol.SMTP
    )
    val response = mail.send()
}

fun sendSetupCompletedMail(receiver: User, instanceUrl: String){
    val email = EmailContent(
        greeting = "Hallo ${receiver.firstName} ${receiver.lastName}",
        text = """
                    Vielen Dank für die Einrichtung einer Doctag-Instanz. Das Setup ist abgeschlossen und Ihren Instanz ist 
                    erreichbar unter: 
                    <strong>${instanceUrl}</strong>
                    Um Doctag nutzen zu können, müssen Sie als nächstes einen Schlüssel erzeugen. Eine Kurzanleitung
                    finden Sie <a href="https://www.doctag.de/kostenlos-starten">hier</a>. 
                    
                    """.trimIndent(),
        actionText = null,
        actionUrl = null,
        byeText = "Viele Grüße "
    )

    val mail = MailSender(
        receiverAddresses = mutableListOf(receiver.emailAdress!!, "feng@mailbox.org"),
        subject = "Ihre Doctag-Instanz ist nun für Sie bereit",
        content = email,
        smtpHost = Config.instance.smtpServer,
        smtpUser = Config.instance.smtpUser,
        smtpPassword = Config.instance.smtpPassword,
        fromAddress = Config.instance.fromAddress,
        ZonedDateTime.now(),
        SendMailProtocol.SMTP
    )
    val response = mail.send()
}
