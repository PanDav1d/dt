package de.doctag.docsrv.ui.document

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
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

/*
fun ElementCreator<*>.handleDocumentList() {
    authRequired {
        val documents = KVar(db().documents.find().sort(descending(Document::created)).toList())
        val isImportRunning = KVar(false)

        pageBorderAndTitle(i18n("ui.document.documentList.header", "Dokumente")) { pageArea ->
            div(fomantic.content).new() {

                render(isImportRunning, container = {div()}){ isRunning->
                documentTabMenu(DocumentTabMenuActiveItem.DocumentList) {
                        val modal = addDocumentModal { doc->
                            logger.info("Adding document")
                            pageArea.showToast(i18n("ui.document.documentList.documentAddedSuccessfullyMessage", "Dokument erfolgreich hinzugefügt"), ToastKind.Success)
                            documents.value = listOf(doc).plus(documents.value)
                        }

                        a(fomantic.item).text(i18n("ui.document.documentList.addDocument","Dokument hinzufügen")).on.click {
                            modal.open()
                        }


                        if(isRunning) {
                            div(fomantic.ui.active.inline.loader)
                        }
                        else {
                            a(fomantic.item).i18nText("ui.document.documentList.importFromEmail", "Aus Mail Postfach importieren").on.click {
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
                                th().i18nText("ui.document.documentList.fileNameLabel","Dateiname")
                                th().i18nText("ui.document.documentList.statusLabel", "Status")
                                th().i18nText("ui.document.documentList.createdLabel", "Erstellt am")
                                th().i18nText("ui.document.documentList.actionLabel", "Aktion")
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
                                            browser.navigateTo("/d/${docIdPart}/${hostname}")
                                        }
                                        else {
                                            browser.navigateTo("/d/${docIdPart}")
                                        }
                                    }
                                }.new {
                                    td().text(document.originalFileName ?: "")
                                    td().new {
                                        document.getWorkflowStatus().forEach { (role, signature) ->
                                            if(signature != null) {
                                                val signedAt = signature.signed?.formatDateTime()
                                                i(fomantic.ui.icon.check.circle.outline.green).withPopup(role, i18n("ui.document.documentList.signedByPopupText", "Signiert am ${signedAt} von ${signature.signedByKey?.ownerAddress?.name1}"))
                                            }
                                            else {
                                                i(fomantic.ui.icon.circle.outline.grey).withPopup(role, i18n("ui.document.documentList.notYetSignedText", "Noch nicht signiert"))
                                            }
                                        }
                                    }
                                    td().text(document.created?.formatDateTime() ?: "")
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
*/