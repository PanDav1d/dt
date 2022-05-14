import de.doctag.docsrv.model.OutboundMailConfig
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender

fun documentWasSignedMail(conf: OutboundMailConfig, toAddress: String, documentUrl: String) : Boolean{
    val email = EmailContent(
        greeting = "Hallo",
        text = """
                    | Ein von Ihnen beobachtetes Dokument wurde signiert. Öffnen Sie das Dokument um weitere Informationen
                    | zur Signatur zu erhalten
                    """.trimMargin(),
        actionText = "Dokument öffnen",
        actionUrl = documentUrl,
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