package de.doctag.docsrv.ui.document

import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.addDocumentModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import java.time.format.DateTimeFormatter


fun ElementCreator<*>.handleDocumentList() {
    authRequired {
        val documents = KVar(db().documents.find().toList())
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
