package de.docward.docsrv.ui.modals

import de.docward.docsrv.DocSignature
import de.docward.docsrv.SignatureLoadingResult
import de.docward.docsrv.formatDateTime
import de.docward.docsrv.model.DbContext
import de.docward.docsrv.remotes.DocServer
import de.docward.docsrv.ui.modal
import de.docward.docsrv.ui.scanQrCode
import de.docward.docsrv.ui.tertiary
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

private fun String.isUrl(): Boolean = this.startsWith("https://")

fun ElementCreator<*>.scanStatusModal(onScanSuccessful: (u: SignatureLoadingResult?)->Unit) = modal("Status erfassen"){ modal->
    val scannedCode = KVar<String>("")

    render(scannedCode){ code->

        when{
            code.isBlank() -> {
                scanQrCode { scanResult->
                    scannedCode.value = scanResult
                }
            }
            code.isUrl() -> {

                val doc = DocServer.loadDocument(code)

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
                }else {
                    logger.info("Found remote document. Allow signing it")
                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).text("Dokument erkannt")
                        div().text("Dateiname: ${doc.originalFileName}")
                        div().text("Erstellt am: ${doc.created?.formatDateTime()}")
                        div().text("Quelle: ${doc.url}")
                    }
                    button(fomantic.ui.button).text("Signieren").on.click {
                        DocServer.signDocument(doc, DbContext.keys.find().first()!!)

                        modal.close()
                    }
                    button(fomantic.ui.button.tertiary.blue).text("Erneut Scannen").on.click {
                        scannedCode.value = ""
                    }
                }
            }
            else -> {
                val sig = DocSignature.load(code)
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
                        onScanSuccessful(sig)
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