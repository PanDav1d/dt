package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.isUrl
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.*
import de.doctag.lib.DoctagSignature
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import java.time.ZonedDateTime

fun ElementCreator<*>.createDocumentSignRequestModal(onCreate: (docSignReq:DocumentSignRequest)->Unit) = modal("Signaturanfrage erstellen") { modal ->

    val scannedCode = KVar("")

    render(scannedCode){ code ->
        when{
            scannedCode.value.isBlank() -> {
                scanQrCode { scanResult->
                    scannedCode.value = scanResult
                }
            }
            code.isUrl() -> {

                val url = code.replace("https://127.0.0.1", "http://127.0.0.1")
                val embeddedDoc = DocServerClient.loadDocument(url)
                val files = embeddedDoc?.files
                val doc = embeddedDoc?.document

                if(doc == null) {
                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).text("URL erkannt")
                        p().text("$code")
                    }
                    button(fomantic.ui.button).text("Öffnen").on.click {
                        browser.url.value = code
                        modal.close()
                    }
                    button(fomantic.ui.button.tertiary.blue).text("Erneut Scannen").on.click {
                        scannedCode.value = ""
                    }
                } else {
                    logger.info("Found remote document. Allow signing it")

                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).text("Dokument erkannt")
                        div().text("Dateiname: ${doc.originalFileName}")
                        div().text("Erstellt am: ${doc.created?.formatDateTime()}")
                        div().text("Quelle: ${doc.url}")
                    }

                    val selection = KVar<String?>(null)
                    val opts = doc.workflow?.actions?.map { it.role to (it.role ?:"") }?.toMap()
                    if(opts != null) {
                        formControl {
                            div(fomantic.ui.field).new {
                                label().text("Rolle wählen")
                                dropdown(opts, selection).onSelect {
                                    selection.value = it
                                }
                            }
                        }
                    }

                    div(fomantic.ui.divider.hidden)

                    button(fomantic.ui.button).text("Signaturanfrage erstellen").on.click {
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
                    button(fomantic.ui.button.tertiary.blue).text("Erneut Scannen").on.click {
                        scannedCode.value = ""
                    }
                }
            }
            else -> {
                val sig = DoctagSignature.load(code)
                if(sig.valid){
                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).text("Signatur gültig")
                        p().text("${sig.publicKey?.owner?.firstName} ${sig.publicKey?.owner?.lastName}")
                        p().text("${sig.publicKey?.issuer?.name1}")
                        p().text("${sig.publicKey?.issuer?.name2}")
                        p().text("${sig.publicKey?.issuer?.street}")
                        p().text("${sig.publicKey?.issuer?.countryCode} - ${sig.publicKey?.issuer?.zipCode} - ${sig.publicKey?.issuer?.city}")
                    }
                }
                else {
                    div(fomantic.ui.message.warning).new {
                        div(fomantic.ui.header).text("Signatur nicht gültig")
                        p().text(sig.message!!)
                    }

                }

                if(sig.valid){
                    button(fomantic.ui.button).text("Übernehmen").on.click {
                        //onScanSuccessful(sig)
                        modal.close()
                    }
                }

                button(fomantic.ui.button.tertiary.blue).text("Erneut Scannen").on.click {
                    scannedCode.value = ""
                }
            }
        }
    }


}
