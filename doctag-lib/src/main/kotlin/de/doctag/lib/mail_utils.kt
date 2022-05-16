package de.doctag.lib

import com.sun.mail.smtp.SMTPTransport
import java.io.File
import java.time.ZonedDateTime
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

enum class SendMailProtocol{
    SMTP,
    SMTPS
}

class MailSender(
        val receiverAddresses: List<String>,
        val subject: String,
        val content: String,
        val smtpHost: String,
        val smtpUser: String?,
        val smtpPassword: String?,
        val fromAddress: String?,
        val date: ZonedDateTime = ZonedDateTime.now(),
        val smtpProtocol: SendMailProtocol?,
        val attachmentFile: File? = null
) {

    constructor(receiverAddresses: List<String>,
                subject: String,
                content: EmailContent,
                smtpHost: String,
                smtpUser: String?,
                smtpPassword: String?,
                fromAddress: String?,
                date: ZonedDateTime = ZonedDateTime.now(),
                smtpProtocol: SendMailProtocol?,
                attachmentFile: File? = null) : this(receiverAddresses, subject, content.asHtml(), smtpHost, smtpUser, smtpPassword, fromAddress, date, smtpProtocol, attachmentFile)

    companion object {
        var isTestModeEnabled = false
        val outgoingEmails = mutableListOf<MailSender>()
    }

    fun send(): Boolean {

        if (isTestModeEnabled) {
            println("testmode is enabled, no mail has been sent. Check outgoingEmails.")
            outgoingEmails.add(this)
            return true
        }

        val props: Properties = System.getProperties()
        props.put("mail.smtps.host", smtpHost)
        props.put("mail.smtps.auth", "true")
        props.put("mail.smtps.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        val session: Session = Session.getInstance(props, null)
        val msg = MimeMessage(session)

        msg.setFrom(InternetAddress(fromAddress))

        val addrs: MutableList<InternetAddress> = mutableListOf()
        receiverAddresses.forEach { t: String? -> addrs.add(InternetAddress(t, false)) }
        msg.setRecipients(Message.RecipientType.TO, addrs.toTypedArray())

        msg.subject = subject

        if (attachmentFile == null) {
            msg.setContent(content, "text/html; charset=utf-8")
        } else {
            val multipart = MimeMultipart()

            val messageBodyPart = MimeBodyPart()
            messageBodyPart.setContent(content, "text/html; charset=utf-8")

            val attachPart = MimeBodyPart()
            attachPart.attachFile(attachmentFile)

            multipart.addBodyPart(messageBodyPart)
            multipart.addBodyPart(attachPart)

            msg.setContent(multipart);
        }

        msg.sentDate = Date.from(date.toInstant())

        System.err.println("##########################Starting to send mail########################## Now: ${System.currentTimeMillis()}")

        val t: SMTPTransport = when(smtpProtocol) {
            SendMailProtocol.SMTPS->session.getTransport("smtps") as SMTPTransport
            else -> session.getTransport("smtp") as SMTPTransport
        }
        t.connect(smtpHost, smtpUser, smtpPassword)
        t.sendMessage(msg, msg.allRecipients)
        val lastServerResponse = t.lastServerResponse
        t.close()

        logger.info("Server response is ${lastServerResponse}")
        return lastServerResponse.contains("250")
    }
}

data class EmailContent(
        val text: String,
        val greeting: String?,
        val actionText: String?,
        val actionUrl: String?,
        val byeText: String?
) {
    fun asHtml(): String {
        return withHtml(this.text, this.greeting, this.actionText, this.actionUrl, this.byeText)
    }
}

fun withHtml(text: String, greeting: String?, actionText: String?, actionUrl: String?, bye: String?): String {
    /*Source https://github.com/leemunroe/responsive-html-email-template */

    val actionHtml = if (actionText == null) "" else {
        """
            <table role="presentation" border="0" cellpadding="0" cellspacing="0" class="btn btn-primary">
              <tbody>
                <tr>
                  <td align="left">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                      <tbody>
                        <tr>
                          <td> <a href="${actionUrl}" target="_blank">$actionText</a> </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
                        </table>
        """.trimIndent()
    }

    return """
        <!doctype html>
<html>
  <head>
    <meta name="viewport" content="width=device-width" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Simple Transactional Email</title>
    <style>
      /* -------------------------------------
          GLOBAL RESETS
      ------------------------------------- */

      /*All the styling goes here*/

      img {
        border: none;
        -ms-interpolation-mode: bicubic;
        max-width: 100%;
      }

      body {
        background-color: #f6f6f6;
        font-family: sans-serif;
        -webkit-font-smoothing: antialiased;
        font-size: 14px;
        line-height: 1.4;
        margin: 0;
        padding: 0;
        -ms-text-size-adjust: 100%;
        -webkit-text-size-adjust: 100%;
      }

      table {
        border-collapse: separate;
        mso-table-lspace: 0pt;
        mso-table-rspace: 0pt;
        width: 100%; }
        table td {
          font-family: sans-serif;
          font-size: 14px;
          vertical-align: top;
      }

      /* -------------------------------------
          BODY & CONTAINER
      ------------------------------------- */

      .body {
        background-color: #f6f6f6;
        width: 100%;
      }

      /* Set a max-width, and make it display as block so it will automatically stretch to that width, but will also shrink down on a phone or something */
      .container {
        display: block;
        Margin: 0 auto !important;
        /* makes it centered */
        max-width: 580px;
        padding: 10px;
        width: 580px;
      }

      /* This should also be a block element, so that it will fill 100% of the .container */
      .content {
        box-sizing: border-box;
        display: block;
        Margin: 0 auto;
        max-width: 580px;
        padding: 10px;
      }

      /* -------------------------------------
          HEADER, FOOTER, MAIN
      ------------------------------------- */
      .main {
        background: #ffffff;
        border-radius: 3px;
        width: 100%;
      }

      .wrapper {
        box-sizing: border-box;
        padding: 20px;
      }

      .content-block {
        padding-bottom: 10px;
        padding-top: 10px;
      }

      .footer {
        clear: both;
        Margin-top: 10px;
        text-align: center;
        width: 100%;
      }
        .footer td,
        .footer p,
        .footer span,
        .footer a {
          color: #999999;
          font-size: 12px;
          text-align: center;
      }

      /* -------------------------------------
          TYPOGRAPHY
      ------------------------------------- */
      h1,
      h2,
      h3,
      h4 {
        color: #000000;
        font-family: sans-serif;
        font-weight: 400;
        line-height: 1.4;
        margin: 0;
        margin-bottom: 30px;
      }

      h1 {
        font-size: 35px;
        font-weight: 300;
        text-align: center;
        text-transform: capitalize;
      }

      p,
      ul,
      ol {
        font-family: sans-serif;
        font-size: 14px;
        font-weight: normal;
        margin: 0;
        margin-bottom: 15px;
      }
        p li,
        ul li,
        ol li {
          list-style-position: inside;
          margin-left: 5px;
      }

      a {
        color: #3498db;
        text-decoration: underline;
      }

      /* -------------------------------------
          BUTTONS
      ------------------------------------- */
      .btn {
        box-sizing: border-box;
        width: 100%; }
        .btn > tbody > tr > td {
          padding-bottom: 15px; }
        .btn table {
          width: auto;
      }
        .btn table td {
          background-color: #ffffff;
          border-radius: 5px;
          text-align: center;
      }
        .btn a {
          background-color: #ffffff;
          border: solid 1px #3498db;
          border-radius: 5px;
          box-sizing: border-box;
          color: #3498db;
          cursor: pointer;
          display: inline-block;
          font-size: 14px;
          font-weight: bold;
          margin: 0;
          padding: 12px 25px;
          text-decoration: none;
          text-transform: capitalize;
      }

      .btn-primary table td {
        background-color: #3498db;
      }

      .btn-primary a {
        background-color: #3498db;
        border-color: #3498db;
        color: #ffffff;
      }

      /* -------------------------------------
          OTHER STYLES THAT MIGHT BE USEFUL
      ------------------------------------- */
      .last {
        margin-bottom: 0;
      }

      .first {
        margin-top: 0;
      }

      .align-center {
        text-align: center;
      }

      .align-right {
        text-align: right;
      }

      .align-left {
        text-align: left;
      }

      .clear {
        clear: both;
      }

      .mt0 {
        margin-top: 0;
      }

      .mb0 {
        margin-bottom: 0;
      }

      .preheader {
        color: transparent;
        display: none;
        height: 0;
        max-height: 0;
        max-width: 0;
        opacity: 0;
        overflow: hidden;
        mso-`-hide`: all;
        visibility: hidden;
        width: 0;
      }

      .powered-by a {
        text-decoration: none;
      }

      hr {
        border: 0;
        border-bottom: 1px solid #f6f6f6;
        Margin: 20px 0;
      }

      /* -------------------------------------
          RESPONSIVE AND MOBILE FRIENDLY STYLES
      ------------------------------------- */
      @media only screen and (max-width: 620px) {
        table[class=body] h1 {
          font-size: 28px !important;
          margin-bottom: 10px !important;
        }
        table[class=body] p,
        table[class=body] ul,
        table[class=body] ol,
        table[class=body] td,
        table[class=body] span,
        table[class=body] a {
          font-size: 16px !important;
        }
        table[class=body] .wrapper,
        table[class=body] .article {
          padding: 10px !important;
        }
        table[class=body] .content {
          padding: 0 !important;
        }
        table[class=body] .container {
          padding: 0 !important;
          width: 100% !important;
        }
        table[class=body] .main {
          border-left-width: 0 !important;
          border-radius: 0 !important;
          border-right-width: 0 !important;
        }
        table[class=body] .btn table {
          width: 100% !important;
        }
        table[class=body] .btn a {
          width: 100% !important;
        }
        table[class=body] .img-responsive {
          height: auto !important;
          max-width: 100% !important;
          width: auto !important;
        }
      }

      /* -------------------------------------
          PRESERVE THESE STYLES IN THE HEAD
      ------------------------------------- */
      @media all {
        .ExternalClass {
          width: 100%;
        }
        .ExternalClass,
        .ExternalClass p,
        .ExternalClass span,
        .ExternalClass font,
        .ExternalClass td,
        .ExternalClass div {
          line-height: 100%;
        }
        .apple-link a {
          color: inherit !important;
          font-family: inherit !important;
          font-size: inherit !important;
          font-weight: inherit !important;
          line-height: inherit !important;
          text-decoration: none !important;
        }
        .btn-primary table td:hover {
          background-color: #34495e !important;
        }
        .btn-primary a:hover {
          background-color: #34495e !important;
          border-color: #34495e !important;
        }
      }

    </style>
  </head>
  <body class="">
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" class="body">
      <tr>
        <td>&nbsp;</td>
        <td class="container">
          <div class="content">

            <!-- START CENTERED WHITE CONTAINER -->
            <span class="preheader">${text.subSequence(0, listOf(text.length, 300, getHtmlTagIndex(text)).min()!!)}</span>
            <table role="presentation" class="main">

              <!-- START MAIN CONTENT AREA -->
              <tr>
                <td class="wrapper">
                  <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                    <tr>
                      <td>
                        <p>${greeting}</p>
                        <p>${text}</p>
                        ${actionHtml}
                        <p>${bye}</p>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>

            <!-- END MAIN CONTENT AREA -->
            </table>

            <!-- START FOOTER -->
            <div class="footer">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td class="content-block">
                    <span class="apple-link">Doctag.de</span>
                    <!-- <br>Keine Emails mehr empfangen? <a href="%unsubscribe_url%" style="color: dark-blue;">abmelden</a>. -->
                  </td>
                </tr>
              </table>
            </div>
            <!-- END FOOTER -->

          <!-- END CENTERED WHITE CONTAINER -->
          </div>
        </td>
        <td>&nbsp;</td>
      </tr>
    </table>
  </body>
</html>
    """.trimIndent()
}

fun getHtmlTagIndex(text: String): Int {
    val index = text.indexOf("<")
    if (index < 0) {
        return Int.MAX_VALUE
    }
    return index
}
