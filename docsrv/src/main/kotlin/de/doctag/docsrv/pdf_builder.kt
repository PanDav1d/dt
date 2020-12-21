package de.doctag.docsrv

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import de.doctag.docsrv.model.Document
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class PdfBuilder(doc: Document)  {

    fun interpolateHtmlTemplate() : String{
        return """<html>
            <head>
            <style>
            body {
              font-family: sans-serif;
            }
            table {
                width: 100%;
            }
            </style>
            </head>
            <body>
            <h3>Warenempfänger</h3>
            <hr/>
            <table>
            <tr>
                <th>Name</th>
                <th>Addresse</th>
                <th>Signiert von</th>
                <th>Datum</th>
            </tr>
            <tr>
                <td>Frank Englert</td>
                <td>Elisabeth-H.Str </td>
                <td>Frank Englert</td>
                <td>21.12.2020</td>
            </tr>
            </table>
            </body>
            </html>""".trimIndent()
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