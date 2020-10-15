package de.doctag.docsrv.ui.document


import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.DocumentSignRequest
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.AttachmentImporter
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.addDocumentModal
import de.doctag.docsrv.ui.modals.createDocumentSignRequestModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.descending
import java.time.format.DateTimeFormatter

private fun ElementCreator<*>.loadDocumentSignRequests() = db().signRequests.find().sort(descending(DocumentSignRequest::timestamp)).toList()

fun ElementCreator<*>.handleSignRequestList() {
    authRequired {
        val requests = KVar(loadDocumentSignRequests())

        pageBorderAndTitle("Signaturanfragen") { pageArea ->
            div(fomantic.content).new() {

                documentTabMenu(DocumentTabMenuActiveItem.SignRequests) {
                    val modal = createDocumentSignRequestModal { docSignRequest ->
                        logger.info("Dokument erfassen")
                        pageArea.showToast("Signaturanfrage erfasst", ToastKind.Success)
                        requests.value = loadDocumentSignRequests()
                    }

                    a(fomantic.item).text("Signaturanfrage erstellen").on.click {
                        modal.open()
                    }
                }


                div(fomantic.ui.divider.hidden)

                render(requests){ rRequests ->
                    logger.info("List of documents did change")
                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().text("Doctag")
                                th().text("Rolle")
                                th().text("Erfasst von")
                                th().text("Erstellt am")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rRequests.forEach { req ->

                                tr().apply {
                                    this.on.click {
                                        logger.info("Clicked")
                                        //browser.url.value="/d/${document._id}"
                                    }
                                }.new {
                                    td().text(req.doctagUrl ?: "")
                                    td().text (req.role ?:"")
                                    td().text (req.createdBy?.userName ?:"")
                                    td().text(req.timestamp?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) ?: "")
                                    td().new {

                                        i(fomantic.ui.key.icon).on.click {
                                            logger.info("Opening document ${req._id}")
                                            browser.url.value = "/doc_sign_requests/${req._id}"
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
