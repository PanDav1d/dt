import de.doctag.docsrv.model.OutboundMailConfig
import de.doctag.lib.EmailContent
import de.doctag.lib.MailSender
import doctag.translation.I18n
import java.util.*

fun documentWasSignedMail(conf: OutboundMailConfig, toAddress: String, documentUrl: String, language: Locale) : Boolean{
    val email = EmailContent(
        greeting = I18n.t("mails.documentWasSignedMail.greeting","Hallo", language= language),
        text = I18n.t("mails.documentWasSignedMail.text","""Ein von Ihnen beobachtetes Dokument wurde signiert. Öffnen Sie das Dokument um weitere Informationen zur Signatur zu erhalten""", language = language),
        actionText = I18n.t("mails.documentWasSignedMail.actionLink","Dokument öffnen", language=language),
        actionUrl = documentUrl,
        byeText = I18n.t("mails.documentWasSignedMail.byeText","Viele Grüße", language=language)
    )

    val ms = MailSender(
        receiverAddresses = listOf(toAddress),
        fromAddress = conf.fromAddress,
        smtpHost = conf.server!!,
        smtpUser = conf.user,
        smtpPassword = conf.password,
        subject = I18n.t("mails.documentWasSignedMail.subject", "Ein von Ihnen beobachtetes Dokument wurde signiert", language = language),
        content = email,
        smtpProtocol = conf.protocol
    )
    return ms.send()
}