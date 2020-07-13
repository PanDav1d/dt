package de.doctag.docsrv.ui.document

import de.doctag.docsrv.extractDocumentIdOrNull
import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.MailReceiver
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.fileExport
import de.doctag.docsrv.ui.modals.addDocumentModal
import de.doctag.docsrv.ui.pageBorderAndTitle
import de.doctag.docsrv.ui.selectable
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.descending
import org.litote.kmongo.save
import org.litote.kmongo.orderBy
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.internet.MimeBodyPart


fun ElementCreator<*>.handleDocumentList() {
    authRequired {
        val documents = KVar(db().documents.find().sort(descending(Document::created)).toList())
        pageBorderAndTitle("Dokumente") { pageArea ->
            div(fomantic.content).new() {

                val modal = addDocumentModal { doc->
                    logger.info("Dokument hinzufügen")
                    pageArea.showToast("Dokument erfolgreich hinzugefügt", ToastKind.Success)
                    documents.value = listOf(doc).plus(documents.value)
                }

                button(fomantic.ui.button).text("Dokument hinzufügen").on.click {
                    modal.open()
                }

                button(fomantic.ui.button).text("Aus Mail Postfach importieren").on.click {
                    db().currentConfig.inboundMail?.let {
                        val recv = MailReceiver.connect(it)
                        val messages = recv?.receive()

                        messages?.filter { it.contentType.contains("multipart") }?.forEach { msg->
                            logger.info ("Checking if message from ${msg.from?.first()} / ${msg.sentDate?.toString()}")
                            val multiPart  = msg.content as Multipart

                            for (i in 0 until multiPart.count) {

                                val part = multiPart.getBodyPart(i) as MimeBodyPart

                                logger.info("Processing multipart $i of type ${part.disposition}")

                                if (Part.ATTACHMENT == part.disposition?.toLowerCase()) {


                                    if(part.fileName.toLowerCase().endsWith(".pdf")){
                                        logger.info("Importing attachment ${part.fileName}")

                                        val tmpFile = File.createTempFile("abc", ".pdf")
                                        try {
                                            part.saveFile(tmpFile)

                                            val documentData = Base64.getEncoder().encodeToString(tmpFile.readBytes())

                                            val doctagId = extractDocumentIdOrNull(documentData, db().currentConfig.hostname)

                                            val fd = FileData(_id = null, name = part.fileName, base64Content = documentData, contentType = "application/pdf")
                                            fd.apply { db().files.save(fd) }

                                            val doc = Document()
                                            doc._id = doctagId
                                            doc.attachmentId = fd._id
                                            doc.originalFileName = part.fileName
                                            doc.created = ZonedDateTime.now()

                                            doc.apply { db().documents.save(doc) }
                                            doc.url = "https://${db().currentConfig.hostname}/d/${doc._id}"

                                            db().documents.save(doc)
                                        }
                                        finally {
                                            tmpFile.delete()
                                        }
                                    }
                                }
                            }

                            recv?.markAsRead(msg)
                        }



                        logger.info ("Processing mails done. Processed ${messages?.size} mails")
                    }

                }

                div(fomantic.ui.divider.hidden)

                render(documents){ rDocuments ->
                    logger.info("List of documents did change")
                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().text("Nr")
                                th().text("Art")
                                th().text("Dateiname")
                                th().text("Erstellt am")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rDocuments.forEach { document ->

                                tr().apply {
                                    this.on.click {
                                        logger.info("Clicked")
                                        browser.url.value="/d/${document._id}"
                                    }
                                }.new {
                                    td().text(document.externalId ?: "")
                                    td().text(document.classifier ?: "")
                                    td().text(document.originalFileName ?: "")
                                    td().text(document.created?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "")
                                    td().new {

                                        i(fomantic.ui.edit.icon).on.click {
                                            logger.info("Opening document ${document._id}")
                                        }

                                        a(href = "/d/${document._id}/download",attributes = mapOf("download" to "", "class" to "actionIcon")).new {
                                            i(fomantic.ui.fileExport.icon)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
