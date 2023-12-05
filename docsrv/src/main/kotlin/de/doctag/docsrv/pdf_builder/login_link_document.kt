package de.doctag.docsrv.pdf_builder

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import de.doctag.docsrv.formatDate
import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.getQRCodeImageAsDataUrl
import de.doctag.docsrv.model.*
import doctag.translation.I18n
import org.litote.kmongo.findOneById
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class LoginLinkDocument(val user: User, val session: Session, val db: DbContext, val language: Locale)  {

    private fun interpolateHtmlTemplate() : String{
        return """<html>
            <head>
            <style>
            @page
            {
                size: 85mm 55mm;
                margin: 0;
            }
            
            body {
              font-family: sans-serif;
            }
            
            table {
               margin: 0px;
               padding: 0px;
            }
            
            h3 {
                margin-bottom: 0mm;
                padding-bottom: 0mm;
            }
            
            table td, table td * {
                vertical-align: top;
            }
            
            .tagline {
                padding:0px;
                margin: 0px;
                font-size: 8pt;
                color: #333333;
            }
            
           .name {
                padding:0px;
                margin: 0px;
                font-size: 10pt;
                font-weight: bold;
           }
           
           .slight {
                padding-top: 2mm;
            }
            </style>
            </head>
            <body>
            <h3>${I18n.t("pdfBuilder.loginLinkDocument.header","Schlüsselkarte", language = language)}</h3>
            <table>
            <tr>
            <td>
            <p class="tagline slight">${I18n.t("pdfBuilder.loginLinkDocument.ownerTitle","INHABER", language = language)}</p>
            <p class="name">${user.firstName} ${user.lastName}</p>
            <p class="tagline slight">${I18n.t("pdfBuilder.loginLinkDocument.ownerEmail","E-MAIL", language = language)}</p>
            <p class="name">${user.emailAdress}</p>
            <p class="tagline slight">${I18n.t("pdfBuilder.loginLinkDocument.vaildTill","GÜLTIG BIS", language = language)}</p>
            <p class="name">${session.expires.formatDate()}</p>
            <p class="tagline slight">${I18n.t("pdfBuilder.loginLinkDocument.validOnInstance","FÜR INSTANZ",language = language)}</p>
            <p class="name">${db.currentConfig.hostname}</p>
            </td>
            </tr>
            </table>
            <div style="position: absolute; right: 2mm; top: 2mm;">${renderLoginQrCode()}</div>
            </body>
            </html>""".trimIndent()
    }

    private fun renderLoginQrCode() : String{
        val loginUrl = "https://${db.currentConfig.hostname}/login/?sessionId=${session.sessionId}"
        val qr = getQRCodeImageAsDataUrl(loginUrl, 400,400, 1)

        return "<img src=\"${qr}\" height=\"30mm\"/>"
    }


    fun render(): ByteArrayOutputStream {

        val html = interpolateHtmlTemplate()

        File("test.html").writeText(html)

        val os = ByteArrayOutputStream()
        val builder = PdfRendererBuilder()
        builder.useFastMode()
        builder.withHtmlContent(html,"/")
        builder.toStream(os)
        builder.run()

        return os
    }

    fun asBase64Str(): String{
        return Base64.getEncoder().encodeToString(this.render().toByteArray())
    }
}