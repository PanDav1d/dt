package de.doctag.docsrv.ui.document

import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.modals.signDocumentModal
import de.doctag.docsrv.ui.pageBorderAndTitle
import de.doctag.docsrv.ui.tertiary
import de.doctag.lib.fixHttps
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import java.time.ZonedDateTime

fun ElementCreator<*>.handleViewSignRequest(id: String) {
    authRequired {
        val signRequest = KVar(db().signRequests.findOneById(id))


        val doc = DocServerClient.loadDocument(signRequest.value?.doctagUrl!!.fixHttps())
        val docUrl = DocumentId.parse(doc?.document?.url!!)
        val previewFileUrl = "https://${docUrl.hostname}/f/${doc.files.first()._id}/view".fixHttps()

        render(signRequest){
            pageBorderAndTitle("Signaturanfrage ansehen") { pageArea ->
                div(fomantic.content).new() {
                    div(fomantic.ui.grid).new(){
                        div(fomantic.twelve.wide.column).new {
                            h4(fomantic.ui.horizontal.divider.header).text("Vorschau")
                            element("iframe", mapOf("style" to "position: absolute; height: 80vh; width:90%; border: none", "src" to previewFileUrl))
                        }
                        div(fomantic.four.wide.column).new {
                            h4(fomantic.ui.horizontal.divider.header).text("Aktionen")

                                when(signRequest.value?.status) {
                                    DocumentSignRequestStatus.SIGNED -> {
                                        div(fomantic.ui.message.green).new {
                                            div(fomantic.header).text("Bereits signiert")
                                            p().text("Diese Signaturanfrage wurde bereits bearbeitet. Sie können die Signatur im Dokument betrachten.")

                                            val docId = DocumentId.parse(doc.document.url!!)
                                            a(
                                                fomantic.ui.button,
                                                href = "/d/${docId.id}/${docId.hostname}"
                                            ).text("Zum Dokument")
                                        }
                                    }
                                    DocumentSignRequestStatus.REJECTED -> {
                                        div(fomantic.ui.message.yellow).new {
                                            div(fomantic.header).text("Abgelehnt")
                                            p().text("Diese Signaturanfrage wurde bereits abgelehnt.")

                                            val docId = DocumentId.parse(doc.document.url!!)
                                            a(
                                                fomantic.ui.button,
                                                href = "/d/${docId.id}/${docId.hostname}"
                                            ).text("Zum Dokument")
                                        }
                                    }
                                    else -> {
                                        div(fomantic.ui.item).new {
                                            val modal = signDocumentModal(doc.document) { signedDocument, addedSignature ->
                                                doc.files.forEach { db().files.save(it) }
                                                db().documents.save(signedDocument)

                                                val files = addedSignature.inputs?.mapNotNull { it.fileId }?.distinct()
                                                    ?.mapNotNull { db().files.findOneById(it) }
                                                val embeddedSignature = EmbeddedSignature(files ?: listOf(), addedSignature)

                                                DocServerClient.pushSignature(signedDocument.url!!, embeddedSignature)

                                                signRequest.value?.let {
                                                    it.status = DocumentSignRequestStatus.SIGNED
                                                    it.signedTimestamp = ZonedDateTime.now()
                                                    db().signRequests.save(it)
                                                }

                                                pageArea.showToast("Dokument signiert", ToastKind.Success)

                                                GlobalScope.launch {
                                                    delay(150)
                                                    signRequest.value = db().signRequests.findOneById(id)
                                                }
                                            }
                                            button(fomantic.ui.button.tertiary.blue).text("Signieren").on.click {
                                                modal.open()
                                            }
                                        }

                                        div(fomantic.ui.item).new {
                                            button(fomantic.ui.button.tertiary.red).text("Ablehnen").on.click {
                                                signRequest.value!!.status = DocumentSignRequestStatus.REJECTED
                                                signRequest.value!!.signedTimestamp = ZonedDateTime.now()
                                                db().signRequests.save(signRequest.value!!)
                                                pageArea.showToast("Anfrage abgelehnt", ToastKind.Error)

                                                GlobalScope.launch {
                                                    delay(150)
                                                    signRequest.value = db().signRequests.findOneById(id)
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
}