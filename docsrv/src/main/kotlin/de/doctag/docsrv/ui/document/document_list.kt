package de.doctag.docsrv.ui.document

import de.doctag.docsrv.extractDocumentIdOrNull
import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.AttachmentImporter
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
import java.time.format.DateTimeFormatter
import de.doctag.docsrv.ui.loading


fun ElementCreator<*>.handleDocumentList() {
    authRequired {
        val documents = KVar(db().documents.find().sort(descending(Document::created)).toList())
        val isImportRunning = KVar(false)

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

                render(isImportRunning){ isRunning->
                    button(fomantic.ui.button.loading(isRunning)).text("Aus Mail Postfach importieren").on.click {
                        try {
                            isImportRunning.value = true
                            AttachmentImporter(db()).runImport()
                        }finally {
                            isImportRunning.value = false
                        }
                    }
                }


                div(fomantic.ui.divider.hidden)

                render(documents){ rDocuments ->
                    logger.info("List of documents did change")
                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
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
