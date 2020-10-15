package de.doctag.docsrv.ui.document

import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.AttachmentImporter
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.addDocumentModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.descending
import java.time.format.DateTimeFormatter


fun ElementCreator<*>.handleDocumentList() {
    authRequired {
        val documents = KVar(db().documents.find().sort(descending(Document::created)).toList())
        val isImportRunning = KVar(false)

        pageBorderAndTitle("Dokumente") { pageArea ->
            div(fomantic.content).new() {

                render(isImportRunning, container = {div()}){ isRunning->
                documentTabMenu(DocumentTabMenuActiveItem.DocumentList) {
                        val modal = addDocumentModal { doc->
                            logger.info("Dokument hinzufügen")
                            pageArea.showToast("Dokument erfolgreich hinzugefügt", ToastKind.Success)
                            documents.value = listOf(doc).plus(documents.value)
                        }

                        a(fomantic.item).text("Dokument hinzufügen").on.click {
                            modal.open()
                        }


                        if(isRunning) {
                            div(fomantic.ui.active.inline.loader)
                        }
                        else {
                            a(fomantic.item).text("Aus Mail Postfach importieren").on.click {
                                try {
                                    isImportRunning.value = true
                                    AttachmentImporter(db()).runImport()
                                } finally {
                                    isImportRunning.value = false
                                }
                            }
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
                                th().text("Status")
                                th().text("Erstellt am")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rDocuments.forEach { document ->

                                tr().apply {
                                    this.on.click {
                                        logger.info("Clicked")
                                        val docIdPart = document.url!!.split("/d/")[1]
                                        val hostname = document.url!!.split("/d/")[0].removePrefix("https://")

                                        if(hostname != db().currentConfig.hostname){
                                            browser.url.value="/d/${docIdPart}/${hostname}"
                                        }
                                        else {
                                            browser.url.value="/d/${docIdPart}"
                                        }
                                    }
                                }.new {
                                    td().text(document.originalFileName ?: "")
                                    td().new {
                                        document.getWorkflowStatus().forEach { (role, signature) ->
                                            if(signature != null) {
                                                val signedAt = signature.signed?.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"))
                                                i(fomantic.ui.icon.check.circle.outline.green).withPopup(role, "Signiert am ${signedAt} von ${signature.publicKey?.issuer?.name1}")
                                            }
                                            else {
                                                i(fomantic.ui.icon.circle.outline.grey).withPopup(role, "Noch nicht signiert")
                                            }
                                        }
                                    }
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
