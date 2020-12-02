package de.doctag.docsrv.ui.document

import de.doctag.docsrv.formatDateTime
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


fun ElementCreator<*>.handleDocumentPreviewList() {
    authRequired {
        val documents = KVar(db().documents.find().sort(descending(Document::created)).toList())
        val isImportRunning = KVar(false)


        val pageArea = pageHeader("Dokumentenliste")

        div(fomantic.ui.main.container).new {

            div(fomantic.ui.grid).new {

                div(fomantic.row).new {
                    div(fomantic.sixteen.wide.column).new {
                        div(fomantic.ui.vertical.segment).new {

                            h1(fomantic.ui.header).text("Dokumentenliste")
                            div(fomantic.ui.content).new {
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
                                }
                            }
                        }
                    }
                }

                div(fomantic.row).new {
                    div(fomantic.four.wide.column).new {
                        render(documents, container = {div()}){ rDocuments ->
                            logger.info("List of documents did change")
                            table(fomantic.ui.selectable.celled.table).new {
                                thead().new {
                                    tr().new {
                                        th().new{
                                            div(fomantic.ui.input.fluid).new() {
                                                input(InputType.text, placeholder = "suche")
                                            }
                                        }
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
                                            td().new {
                                                div().text(document.originalFileName?.take(35) ?: "")
                                                document.getWorkflowStatus().forEach { (role, signature) ->
                                                    if(signature != null) {
                                                        val signedAt = signature.signed?.formatDateTime()
                                                        i(fomantic.ui.icon.check.circle.outline.green).withPopup(role, "Signiert am ${signedAt} von ${signature.publicKey?.issuer?.name1}")
                                                    }
                                                    else {
                                                        i(fomantic.ui.icon.circle.outline.grey).withPopup(role, "Noch nicht signiert")
                                                    }
                                                }
                                                span(mapOf("style" to "float:right")).text(document.created?.formatDateTime(true) ?: "")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    div(fomantic.twelve.wide.column).new {
                        h1().text("Preview goes hiere")
                    }
                }
            }
        }
    }
}
