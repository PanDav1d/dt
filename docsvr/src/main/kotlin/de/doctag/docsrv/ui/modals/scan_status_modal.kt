package de.doctag.docsrv.ui.modals


import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.scanQrCode
import de.doctag.docsrv.ui.tertiary
import de.doctag.lib.DoctagSignature
import de.doctag.lib.SignatureLoadingResult
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

                val doc = DocServerClient.loadDocument(code)

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
                        DocServerClient.signDocument(doc, DbContext.keys.find().first()!!)

                        modal.close()
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