package de.doctag.docsrv

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import de.doctag.docsrv.model.*
import org.litote.kmongo.findOneById
import java.io.ByteArrayOutputStream


class PdfBuilder(val doc: Document, val db: DbContext)  {

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
            </style>
            </head>
            <body>
            <h1>Signaturen</h1>
            ${doc.signatures?.map { sig-> interpolateSignature2(sig)}?.joinToString("\n") ?: "Noch keine Signaturen vorhanden"}
            </body>
            </html>""".trimIndent()
    }

    private fun renderWorkflowInput(input: WorkflowInputResult): String  {

        val value = when{
            input.fileId != null -> {
                val attachment = db.files.findOneById(input.fileId!!)
                if(attachment?.contentType?.contains("image") == true){
                    "<img src=\"data:${attachment.contentType};base64,${attachment.base64Content}\"/>"
                }
                else {
                    "Datei ${attachment?.name}"
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

    private fun interpolateSignature2(sig: Signature)  : String{
        return """
            <br/>
            <br/>
            <div class="sig-section">
            <h3>${sig.role}</h3>
        <hr/>
        <table>
        <tr>
        <td class="tbl-key">Datum</td>
        <td>${sig.signed?.formatDateTime(false)}</td>
        </tr>
        <tr>
        <td class="tbl-key">Name</td>
        <td>${sig.doc?.signingUser}</td>
        </tr>
        <tr>
        <td class="tbl-key">Addresse</td>
        <td>
            ${sig.publicKey?.ownerAddress?.name1}<br />
            ${sig.publicKey?.ownerAddress?.name2?.plus("<br/>") ?:""}
            ${sig.publicKey?.ownerAddress?.street?.plus("<br/>")}
            ${sig.publicKey?.ownerAddress?.countryCode} - ${sig.publicKey?.ownerAddress?.zipCode} ${sig.publicKey?.ownerAddress?.city}<br />
        </td>
        </tr>
        <tr>
        <td class="tbl-key">Doctag-System</td>
        <td>${sig.doc?.signingDoctagInstance}</td>
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