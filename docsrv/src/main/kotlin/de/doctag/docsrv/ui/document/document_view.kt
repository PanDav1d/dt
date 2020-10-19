package de.doctag.docsrv.ui.document

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.getQRCodeImageAsDataUrl
import de.doctag.docsrv.model.*
import org.litote.kmongo.findOneById
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.filePreviewModal
import de.doctag.docsrv.ui.modals.scanStatusModal
import de.doctag.docsrv.ui.modals.signDocumentModal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.save
import java.time.ZonedDateTime


fun ElementCreator<*>.handleDocument(docId: String?, hostname: String?) {

    val document = KVar(db().documents.findOne(Document::url eq "https://${hostname ?: db().currentConfig.hostname}/d/${docId}")!!)
    val selectedSignature = KVar<Int?>(null)
    val isSignatureValid = KVar<Boolean?>(null)
    GlobalScope.launch { delay(500); isSignatureValid.value = document.value.validateSignatures(db()) }

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
                                    div(fomantic.description).text("Erstellungsdatum")
                                }
                            }

                            div(fomantic.ui.item).new {
                                i(fomantic.ui.tags.icon)
                                div(fomantic.ui.content).new {
                                    span(fomantic.header).new {
                                        span().text("${rDocument.url ?: ""} ")
                                        a(href="#", attributes = mapOf("style" to "color:black;")).new {
                                            i(fomantic.icon.eye).on.click {
                                                filePreviewModal(db().files.findOneById(rDocument.attachmentId!!)!!).open()
                                            }
                                        }
                                    }
                                    div(fomantic.description).text("Dokumentenaddresse")
                                }
                            }

                            div(fomantic.ui.item).new {
                                i(fomantic.ui.eye.outline.icon)
                                div(fomantic.ui.content).new {
                                    span(fomantic.header).new {
                                        span().text("Beobachter ")
                                        div(fomantic.ui.mini.label).text((rDocument.mirrors?.size ?: 0).toString())
                                    }
                                    div(fomantic.description).new{
                                        a().text("hinzufügen")
                                    }
                                }
                            }

                            render(isSignatureValid, container = {div(fomantic.ui.item)}){validationResult->

                                when(validationResult){
                                    true  -> i(fomantic.icon.smile.outline)
                                    false -> i(fomantic.icon.exclamationCircle)
                                    null -> div(fomantic.ui.active.mini.inline.loader).apply { setAttributeRaw("style", "display: inline;") }

                                }

                                div(fomantic.ui.content).also {
                                    it.setAttributeRaw("style", "margin-left:24px;")
                                }.new {

                                    span(fomantic.header).new {
                                        when(validationResult){
                                            true -> span().text("Signaturen gültig ")
                                            false -> span().text("Signaturen ungültig ")
                                            null -> span().text("Validierung läuft ")
                                        }

                                    }
                                    div(fomantic.description).new{
                                        div(fomantic.description).text("Status")
                                    }
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
                                        db().documents.save(rDocument)

                                        logger.info("Captured signature and saved to db")
                                        pageArea.showToast("Status erfolgreich erfasst", ToastKind.Success)
                                    }
                                }

                                button(fomantic.ui.button.tertiary.blue).text("Status erfassen").on.click {
                                    modal.open()
                                }
                            }
                        }
                        if(this.browser.authenticatedUser != null) {
                            div(fomantic.ui.item).new {
                                val modal = signDocumentModal(rDocument){signedDocument,_->
                                    db().documents.save(signedDocument)
                                    document.value = signedDocument
                                }
                                button(fomantic.ui.button.tertiary.blue).text("Signieren").on.click {
                                    modal.open()
                                }
                            }
                        }

                        div(fomantic.ui.item).new {
                            a(href = "/d/${rDocument._id}/download", attributes = mapOf("download" to "", "class" to "ui button tertiary blue")).text("Herunterladen")
                        }
                    }
                }

                h2(fomantic.ui.header).new {
                    span().text("Signaturen")
                }


                if(rDocument.signatures?.count() ?: 0 > 0 || rDocument.workflow != null) {
                    render(selectedSignature){selectedSignatureIdx ->
                        table(fomantic.ui.very.basic.selectable.table.compact).new {
                            thead().new {
                                tr().new {
                                    th().text("Rolle")
                                    th().text("Name")
                                    th().text("Addresse")
                                    th().text("Signiert von")
                                    th().text("Datum")
                                    th().text("Aktion")
                                }
                            }
                            tbody().new {
                                rDocument.signatures?.forEachIndexed { idx, sig ->

                                    tr().apply {
                                        on.click {
                                            if(selectedSignature.value == idx){
                                                selectedSignature.value = null
                                            }else {
                                                selectedSignature.value = idx
                                            }
                                        }
                                    }.new {
                                        td().text(sig.role ?: "n.v.")
                                        td().text(sig.publicKey?.issuer?.name1 ?: "")
                                        td().text("${sig.publicKey?.issuer?.zipCode ?: ""} ${sig.publicKey?.issuer?.city ?: ""}")
                                        td().text("${sig.publicKey?.owner?.firstName} ${sig.publicKey?.owner?.lastName}")
                                        td().text(sig.signed?.formatDateTime() ?: "")
                                        td().new {
                                        }
                                    }

                                    if(selectedSignatureIdx == idx) {
                                        tr().new {
                                            td()
                                            td(attributes = mapOf("colspan" to "5")).new {
                                                if(sig.inputs != null) {
                                                    table(fomantic.ui.very.basic.very.compact.collapsing.celled.table).new {
                                                        thead().new {
                                                            tr().new {
                                                                th().text("Name")
                                                                th().text("Wert")
                                                            }
                                                        }
                                                        tbody().new {
                                                            sig.inputs?.forEach { workflowResult ->
                                                                tr().new {
                                                                    td().text(workflowResult.name ?: "")

                                                                    when {
                                                                        workflowResult.value != null -> td().text(workflowResult.value
                                                                                ?: "")
                                                                        workflowResult.fileId != null -> {
                                                                            val fd = db().files.findOneById(workflowResult.fileId!!)
                                                                            td().new{
                                                                                span().text("${fd?.name}  ")
                                                                                i(fomantic.icon.eye).on.click {
                                                                                    filePreviewModal(fd!!).open()
                                                                                }
                                                                            }
                                                                        }
                                                                        else -> td().text("")
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                else {
                                                    span(fomantic.ui.grey.text).text("Keine Workflow-Eingaben vorhanden")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            tbody().new {
                                val missingActions = rDocument.getWorkflowStatus()
                                        .filter { it.second == null }
                                        .map { (currRole, sig) -> rDocument.workflow?.actions?.find { it.role ==  currRole} }

                                missingActions.forEach { missingAction ->
                                    tr().new {
                                        td().text(missingAction?.role ?: "")
                                        td(attributes = mapOf("colspan" to "5")).new {
                                            span(fomantic.ui.grey.text).text("Noch keine Signatur vorhanden")
                                        }
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
