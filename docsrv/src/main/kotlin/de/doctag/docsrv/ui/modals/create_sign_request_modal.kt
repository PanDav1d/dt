package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.*
import de.doctag.lib.DoctagSignatureData
import de.doctag.lib.fixHttps
import de.doctag.lib.isUrl
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import java.time.ZonedDateTime

fun ElementCreator<*>.createDocumentSignRequestModal(onCreate: (docSignReq:DocumentSignRequest)->Unit) = modal(i18n("ui.modals.createSignRequest.title","Signaturanfrage erstellen")) { modal ->

    val scannedCode = KVar("")

    val rescanButtonText = i18n("ui.modals.createSignRequest.reScanButton","Erneut Scannen")

    render(scannedCode){ code ->
        when{
            scannedCode.value.isBlank() -> {
                scanQrCode { scanResult->
                    scannedCode.value = scanResult
                }
            }
            code.isUrl() -> {

                val url = code.fixHttps()
                val embeddedDoc = DocServerClient.loadDocument(url)
                val files = embeddedDoc?.files
                val doc = embeddedDoc?.document

                if(doc == null) {
                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).i18nText("ui.modals.createSignRequest.detectedUrl","URL erkannt")
                        p().text("$code")
                    }
                    button(fomantic.ui.button).i18nText("ui.modals.createSignRequest.openButton","Öffnen").on.click {
                        browser.url.value = code
                        modal.close()
                    }
                    button(fomantic.ui.button.tertiary.blue).text(rescanButtonText).on.click {
                        scannedCode.value = ""
                    }
                } else {
                    logger.info("Found remote document. Allow signing it")

                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).i18nText("ui.modals.createSignRequest.foundDocumentMessage","Dokument erkannt")
                        div().i18nText("ui.modals.createSignRequest.fileName","Dateiname: ${doc.originalFileName}")
                        div().i18nText("ui.modals.createSignRequest.creationDate","Erstellt am: ${doc.created?.formatDateTime()}")
                        div().i18nText("ui.modals.createSignRequest.source","Quelle: ${doc.url}")
                    }

                    val selection = KVar<String?>(null)
                    val opts = doc.workflow?.actions?.map { it.role to (it.role ?:"") }?.toMap()
                    if(opts != null) {
                        formControl {
                            div(fomantic.ui.field).new {
                                label().i18nText("ui.modals.createSignRequest.selectRoleLabel","Rolle wählen")
                                dropdown(opts, selection).onSelect {
                                    selection.value = it
                                }
                            }
                        }
                    }

                    div(fomantic.ui.divider.hidden)

                    button(fomantic.ui.button).i18nText("ui.modals.createSignRequest.signatureRequestCreatedMessage","Signaturanfrage erstellen").on.click {
                        logger.info("Creating document sign reqeust")

                        val docSignRequest = DocumentSignRequest(
                                doctagUrl = doc.url,
                                createdBy = DocumentSignRequestUser(
                                        userId = this.browser.authenticatedUser?._id,
                                        userName = this.browser.authenticatedUser?.firstName + " " + this.browser.authenticatedUser?.lastName
                                ),
                                timestamp = ZonedDateTime.now(),
                                role = selection.value
                        )
                        docSignRequest.apply { db().signRequests.insertOne(docSignRequest)}
                        logger.info("Created document sign request")
                        modal.close()
                        onCreate(docSignRequest)
                    }
                    button(fomantic.ui.button.tertiary.blue).text(rescanButtonText).on.click {
                        scannedCode.value = ""
                    }
                }
            }
            else -> {
                val sig = DoctagSignatureData.load(code)
                if(sig.valid){
                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).i18nText("ui.modals.createSignRequest.signatureValidMessage","Signatur gültig")
                        p().text("${sig.publicKey?.owner?.firstName} ${sig.publicKey?.owner?.lastName}")
                        p().text("${sig.publicKey?.ownerAddress?.name1}")
                        p().text("${sig.publicKey?.ownerAddress?.name2}")
                        p().text("${sig.publicKey?.ownerAddress?.street}")
                        p().text("${sig.publicKey?.ownerAddress?.countryCode} - ${sig.publicKey?.ownerAddress?.zipCode} - ${sig.publicKey?.ownerAddress?.city}")
                    }
                }
                else {
                    div(fomantic.ui.message.warning).new {
                        div(fomantic.ui.header).i18nText("ui.modals.createSignRequest.signatureNotValid","Signatur nicht gültig")
                        p().text(sig.message!!)
                    }

                }

                if(sig.valid){
                    button(fomantic.ui.button).i18nText("ui.modals.createSignRequest.confirmButton","Übernehmen").on.click {
                        //onScanSuccessful(sig)
                        modal.close()
                    }
                }

                button(fomantic.ui.button.tertiary.blue).text(rescanButtonText).on.click {
                    scannedCode.value = ""
                }
            }
        }
    }


}
