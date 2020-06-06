package de.doctag.docsrv.ui.document

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.getQRCodeImageAsDataUrl
import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.authenticatedUser
import org.litote.kmongo.findOneById
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.Signature
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.scanStatusModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save
import java.time.ZonedDateTime


fun ElementCreator<*>.handleDocument(docId: String?) {

    val document = KVar(DbContext.documents.findOneById(docId!!)!!)
    pageBorderAndTitle("Dokument") { pageArea ->

        div(fomantic.content).new() {

            render(document){ rDocument: Document ->
                logger.info("List of documents did change")

                div(fomantic.ui.stackable.two.column.grid).new{
                    div(fomantic.column).new {
                        h4(fomantic.ui.horizontal.divider.header).text("Infos")
                        div(fomantic.ui.relaxed.list).new {
                            div(fomantic.ui.item).new {
                                i(fomantic.ui.paperclip.icon)
                                div(fomantic.ui.content).new {
                                    span(fomantic.header).text(rDocument.originalFileName ?: "")
                                    div(fomantic.description).text("Dateiname")
                                }
                            }
                            div(fomantic.ui.item).new {
                                i(fomantic.ui.calendarDay.icon)
                                div(fomantic.ui.content).new {
                                    span(fomantic.header).text(rDocument.created?.formatDateTime() ?: "")
                                    div(fomantic.description).text("Erstellt am")
                                }
                            }
                            div(fomantic.ui.item).new {
                                i(fomantic.ui.tags.icon)
                                div(fomantic.ui.content).new {
                                    span(fomantic.header).text(rDocument.classifier ?: "")
                                    div(fomantic.description).text("Dokumentenklasse")
                                }
                            }
                            div(fomantic.ui.item).new {
                                i(fomantic.addressCard.icon)
                                div(fomantic.ui.content).new {
                                    span(fomantic.header).text(rDocument.externalId ?: "")
                                    div(fomantic.description).text("Referenz")
                                }
                            }
                        }
                    }

                    div(fomantic.three.wide.column.right.floated).new {
                        h4(fomantic.ui.horizontal.divider.header).text("Aktionen")

                        rDocument.url?.let {url->

                            val m = modal("Dokumentenaddresse"){
                                img(src= getQRCodeImageAsDataUrl(url, 400,400))
                                a(href=url).text(url)
                            }

                            div(fomantic.ui.item).new {
                                button(fomantic.ui.button.tertiary.blue).text("Doctag anzeigen").on.click {
                                    m.open()
                                }
                            }
                        }

                        div(fomantic.ui.item).new {
                            if(this.browser.authenticatedUser != null){
                                val modal = scanStatusModal { sig->
                                    if(sig != null) {
                                        val sigObj = Signature(sig.signedMessage!!, sig.publicKey!!, ZonedDateTime.now(), sig.rawSignedMessage)
                                        rDocument.signatures = (rDocument.signatures ?: listOf()).plus(sigObj)
                                        DbContext.documents.save(rDocument)

                                        logger.info("Captured signature and saved to db")
                                        pageArea.showToast("Status erfolgreich erfasst", ToastKind.Success)
                                    }
                                }

                                button(fomantic.ui.button.tertiary.blue).text("Status erfassen").on.click {
                                    modal.open()
                                }
                            }
                        }
                        div(fomantic.ui.item).new {
                            a(href = "/d/${rDocument._id}/download", attributes = mapOf("download" to "", "class" to "ui button tertiary blue")).text("Herunterladen")
                        }
                    }
                }

                h2(fomantic.ui.header).text("Signaturen")

                if(rDocument.signatures?.count() ?: 0 > 0) {
                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().text("Name")
                                th().text("Ort")
                                th().text("Signiert von")
                                th().text("Signiert am")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rDocument.signatures?.forEach { sig ->

                                tr().new {
                                    td().text(sig.publicKey.issuer.name1 ?: "")
                                    td().text("${sig.publicKey.issuer.zipCode ?: ""} ${sig.publicKey.issuer.city ?: ""}")
                                    td().text("${sig.publicKey.owner.firstName} ${sig.publicKey.owner.lastName}")
                                    td().text(sig.signed.formatDateTime() ?: "")
                                    td().new {

                                        /*
                                        i(fomantic.ui.edit.icon).on.click {
                                            logger.info("Opening document ${document._id}")
                                        }

                                        a(href = "/d/${document._id}/download",attributes = mapOf("download" to "", "class" to "actionIcon")).new {
                                            i(fomantic.ui.fileExport.icon)
                                        }

                                         */
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    p().text("Keine Signaturen vorhanden")
                }
            }
        }
    }
}
