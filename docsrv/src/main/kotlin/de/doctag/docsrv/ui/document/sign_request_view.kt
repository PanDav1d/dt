package de.doctag.docsrv.ui.document

import de.doctag.docsrv.model.DocumentId
import de.doctag.docsrv.model.EmbeddedSignature
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.modals.signDocumentModal
import de.doctag.docsrv.ui.pageBorderAndTitle
import de.doctag.docsrv.ui.tertiary
import de.doctag.lib.fixHttps
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import org.litote.kmongo.findOneById
import org.litote.kmongo.save

fun ElementCreator<*>.handleViewSignRequest(id: String) {
    authRequired {
        val requests = KVar(db().signRequests.findOneById(id))

        val doc = DocServerClient.loadDocument(requests.value?.doctagUrl!!.fixHttps())
        val docUrl = DocumentId.parse(doc?.document?.url!!)
        val previewFileUrl = "https://${docUrl.hostname}/f/${doc.files.first()._id}/view".fixHttps()

        pageBorderAndTitle("Signaturanfrage ansehen") { pageArea ->
            div(fomantic.content).new() {
                div(fomantic.ui.grid).new(){
                    div(fomantic.twelve.wide.column).new {
                        h4(fomantic.ui.horizontal.divider.header).text("Vorschau")
                        element("iframe", mapOf("style" to "position: absolute; height: 80vh; width:90%; border: none", "src" to previewFileUrl))
                    }
                    div(fomantic.four.wide.column).new {
                        h4(fomantic.ui.horizontal.divider.header).text("Aktionen")

                        if(requests.value?.signed == true){

                            div(fomantic.ui.message.green).new {
                                div(fomantic.header).text("Bereits signiert")
                                p().text("Diese Signaturanfrage wurde bereits bearbeitet. Sie können die Signatur im Dokument betrachten.")

                                val docId = DocumentId.parse(doc.document.url!!)
                                a(fomantic.ui.button, href = "/d/${docId.id}/${docId.hostname}").text("Zum Dokument")
                            }
                        }
                        else {
                            div(fomantic.ui.item).new {
                                val modal = signDocumentModal(doc.document) { signedDocument, addedSignature ->
                                    doc.files.forEach { db().files.save(it) }
                                    db().documents.save(signedDocument)

                                    val files = addedSignature.inputs?.mapNotNull { it.fileId }?.distinct()?.mapNotNull { db().files.findOneById(it) }
                                    val embeddedSignature = EmbeddedSignature(files ?: listOf(), addedSignature)

                                    DocServerClient.pushSignature(signedDocument.url!!, embeddedSignature)

                                    requests.value?.let {
                                        it.signed = true
                                        db().signRequests.save(it)
                                    }


                                    pageArea.showToast("Dokument signiert", ToastKind.Success)
                                }
                                button(fomantic.ui.button.tertiary.blue).text("Signieren").on.click {
                                    modal.open()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}