package de.doctag.docsrvHostingController


import de.doctag.docsrv.model.User
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender


fun sendServerPinMail(receiver: User, serverpin: String){
    val email = EmailContent(
            greeting = "Hallo ${receiver.firstName} ${receiver.lastName}",
            text = """
                    | Die für die Einrichtung Ihres Doc-Servers notwendige Server-Pin lautet:
                    | <strong>${serverpin}</strong>
                    | Bitte geben Sie diesen Server-Pin auf der Setup-Seite ein, um die Einrichtung fortzusetzen.""".trimMargin(),
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
            fromAddress = Config.instance.fromAddress
    )
    val response = mail.send()
}

