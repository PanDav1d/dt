package de.doctag.docsrv

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import de.doctag.docsrv.model.*
import doctag.translation.I18n
import org.litote.kmongo.findOneById
import java.io.ByteArrayOutputStream
import java.util.Locale


class PdfBuilder(val doc: Document, val db: DbContext, val language: Locale)  {

    private fun interpolateHtmlTemplate() : String{
        return """<html>
            <head>
            <style>
            body {
              font-family: sans-serif;
            }
            table {
                width: 100%;
            }
            .sig-section {
                page-break-inside: avoid;
            }
            .tbl-key {
                width: 200px;
                font-weight: bold;
            }
            .systemInfoTable {
                font-size: 7pt;
            }
            
            table td, table td * {
                vertical-align: top;
            }
            
            .signatureInfoTable {
                font-size: 10pt;
            }
            </style>
            </head>
            <body>
            ${doc.signatures?.mapIndexed { index, sig-> interpolateSignature2(index, sig)}?.joinToString("\n") ?: I18n.t("pdfBuilder.noSignaturesAvailableMessage","Noch keine Signaturen vorhanden", language = language)}
            </body>
            </html>""".trimIndent()
    }

    private fun renderWorkflowInput(input: WorkflowInputResult): String  {

        val value = when{
            input.fileId != null -> {
                val attachment = db.files.findOneById(input.fileId!!)
                if(attachment?.contentType?.contains("image") == true){
                    "<img src=\"data:${attachment.contentType};base64,${attachment.base64Content}\" height=\"80px\"/>"
                }
                else {
                    I18n.t("pdfBuilder.file","Datei ${attachment?.name}", language = language)
                }
            }
            input.value != null -> input.value
            else -> "---"
        }

        return """
            <tr>
            <td class="tbl-key">
                ${input.name}
            </td>
            <td>
                $value
            </td>
            </tr>
        """
    }

    private fun interpolateSignature2(idx: Int, sig: Signature)  : String{
        return """
            <div class="sig-section" ${if(idx>0)"style=\"page-break-before: always;\"" else ""}>
            <h3>${sig.role}</h3>
        <hr/>
        <h4>${I18n.t("pdfBuilder.technicalInfos", "Technische Informationen", language=language)}</h4>
        <table class="systemInfoTable">
         <tr>
            <td class="tbl-key">${I18n.t("pdfBuilder.operator","Systembetreiber", language=language)}</td>
            <td class="tbl-key">${I18n.t("pdfBuilder.responsibleSystem","verantwortlicher Doctag-Server", language=language)}</td>
            <td class="tbl-key">${I18n.t("pdfBuilder.signatureInfos","Angaben zur Signatur", language=language)}</td>
        </tr>
        <tr>
            <td>
                ${sig.signedByKey?.ownerAddress?.name1}<br />
                ${sig.signedByKey?.ownerAddress?.name2?.plus("<br/>") ?:""}
                ${sig.signedByKey?.ownerAddress?.street?.plus("<br/>")}
                ${sig.signedByKey?.ownerAddress?.countryCode} - ${sig.signedByKey?.ownerAddress?.zipCode} ${sig.signedByKey?.ownerAddress?.city}<br />
            </td>
            <td>${sig.data?.signingDoctagInstance}<br/>${I18n.t("pdfBuilder.publicKey","Öffentlicher Schlüssel:", language=language)} ${sig.signedByKey?.publicKey?.take(16)+"..."}<br/></td>
            <td>${I18n.t("pdfBuilder.checksum","Prüfsumme", language)}: ${sig.data?.documentHash?.take(16)}<br/>${I18n.t("pdfBuilder.signatureIdxOfN","Signatur ${idx+1} von ${doc.signatures?.size}", language=language)}<br/></td>
        </tr>
        </table>
        
        <br/>
        <br/>
        <h4>${I18n.t("pdfBuilder.userInput","Benutzereingaben", language=language)}</h4>
        <table class="signatureInfoTable">
        <tr>
            <td class="tbl-key">${I18n.t("pdfBuilder.date","Datum", language=language)}</td>
            <td>${sig.signed?.formatDateTime(false)}</td>
        </tr>
        <tr>
            <td class="tbl-key">${I18n.t("pdfBuilder.user", "Benutzer", language=language)}</td>
            <td>${sig.data?.signingUser}</td>
        </tr>
        ${sig?.inputs?.map { renderWorkflowInput(it)}?.joinToString("\n") ?: ""}
        </table>
        </div>
        """
    }

    fun render(): ByteArrayOutputStream {
        val os = ByteArrayOutputStream()
        val builder = PdfRendererBuilder()
        builder.useFastMode()
        builder.withHtmlContent(interpolateHtmlTemplate(),"/")
        builder.toStream(os)
        builder.run()

        return os
    }
}