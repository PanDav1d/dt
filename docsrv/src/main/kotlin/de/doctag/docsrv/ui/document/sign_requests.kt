package de.doctag.docsrv.ui.document


import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
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

        pageBorderAndTitle(i18n("ui.document.signRequests.title", "Signaturanfragen")) { pageArea ->
            div(fomantic.content).new() {

                documentTabMenu(DocumentTabMenuActiveItem.SignRequests) {
                    val modal = createDocumentSignRequestModal { docSignRequest ->
                        logger.info("Add signature request")
                        pageArea.showToast(i18n("ui.document.signRequests.addedSignatureRequestSuccessMessage","Signaturanfrage erfasst"), ToastKind.Success)
                        requests.value = loadDocumentSignRequests()
                    }

                    a(fomantic.item).i18nText("ui.document.signRequests.addSignatureRequestButton","Signaturanfrage erstellen").on.click {
                        modal.open()
                    }
                }


                div(fomantic.ui.divider.hidden)

                render(requests){ rRequests ->
                    logger.info("List of documents did change")
                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().i18nText("ui.document.signRequests.documentUrl","Doctag")
                                th().i18nText("ui.document.signRequests.requestedRole","Rolle")
                                th().i18nText("ui.document.signRequests.createdBy","Erfasst von")
                                th().i18nText("ui.document.signRequests.creationDate","Erstellt am")
                                th().i18nText("ui.document.signRequests.status","Status")
                                th().i18nText("ui.document.signRequests.actions","Aktion")
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
                                    td().text(req.timestamp?.formatDateTime() ?: "")
                                    td().new {
                                        when(req.status){
                                            DocumentSignRequestStatus.REQUESTED -> span().i18nText("ui.document.signRequests.requestedStatus","Angefragt")
                                            DocumentSignRequestStatus.REJECTED -> span().i18nText("ui.document.signRequests.rejectedStatus","Abgelehnt")
                                            DocumentSignRequestStatus.SIGNED -> span().i18nText("ui.document.signRequests.signedStatus","Signiert")
                                        }
                                    }
                                    td().new {

                                        i(fomantic.ui.key.icon).on.click {
                                            logger.info("Opening document ${req._id}")
                                            browser.navigateTo("/doc_sign_requests/${req._id}")
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
