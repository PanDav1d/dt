package de.doctag.docsrv.ui.modals


import de.doctag.docsrv.api.EmbeddedDocument
import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.scanQrCode
import de.doctag.docsrv.ui.tertiary
import de.doctag.lib.isUrl
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

enum class SelectedAction{
    SIGN_DOCUMENT,
    CREATE_SIGN_REQUEST
}

data class ScanDocTagResult(
        val selectedAction: SelectedAction,
        val document: EmbeddedDocument
)

fun ElementCreator<*>.scanDoctagModal(onScanSuccessful: (u: ScanDocTagResult)->Unit) = modal(i18n("ui.modals.scanDoctagModal.title","Status erfassen")){ modal->
    val scannedCode = KVar("")
    val rescanButtonText = i18n("ui.modals.scanDoctagModal.rescanButton","Erneut Scannen")
    render(scannedCode){ code->

        when{
            code.isBlank() -> {
                scanQrCode { scanResult->
                    scannedCode.value = scanResult
                }
            }
            code.isUrl() -> {

                val embeddedDoc = DocServerClient.loadDocument(code)
                val doc = embeddedDoc?.document

                if(doc == null) {
                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).i18nText("ui.modals.scanDoctagModal.urlDetectedMessage","URL erkannt")
                        p().text("$code")
                    }
                    button(fomantic.ui.button).i18nText("ui.modals.scanDoctagModal.openButton","Öffnen").on.click {
                        browser.url.value = code
                        modal.close()
                    }
                    button(fomantic.ui.button.tertiary.blue).text(rescanButtonText).on.click {
                        scannedCode.value = ""
                    }
                }else {
                    logger.info("Found remote document. Allow signing it")
                    div(fomantic.ui.message.success).new {
                        div(fomantic.ui.header).i18nText("ui.modals.scanDoctagModal.documentDetectedMessage","Dokument erkannt")
                        div().i18nText("ui.modals.scanDoctagModal.fileName","Dateiname: ${doc.originalFileName}")
                        div().i18nText("ui.modals.scanDoctagModal.createdDate","Erstellt am: ${doc.created?.formatDateTime()}")
                        div().i18nText("ui.modals.scanDoctagModal.source","Quelle: ${doc.url}")
                    }
                    button(fomantic.ui.button).i18nText("ui.modals.scanDoctagModal.signButton","Signieren").on.click {
                        logger.info("Signing document")
                        logger.info("Embedded document contains the following files:")
                        onScanSuccessful(ScanDocTagResult(SelectedAction.SIGN_DOCUMENT, embeddedDoc))
                        modal.close()
                    }
                    button(fomantic.ui.tertiary.button).i18nText("ui.modals.scanDoctagModal.signRequestButton","Signaturanfrage").on.click {
                        onScanSuccessful(ScanDocTagResult(SelectedAction.CREATE_SIGN_REQUEST, embeddedDoc))
                        logger.info("Created Signature Request")
                        modal.close()
                    }
                    button(fomantic.ui.button.tertiary.blue).text(rescanButtonText).on.click {
                        scannedCode.value = ""
                    }
                }
            }
            else -> {

                div(fomantic.ui.message.warning).new {
                    div(fomantic.ui.header).text("Signatur nicht gültig")
                    p().text("Zu dem gescannten QR-Code konnte kein gültiges Dokument erkannt werden. Bitte versuchen Sie es erneut.")
                }

                button(fomantic.ui.button.blue).text(rescanButtonText).on.click {
                    scannedCode.value = ""
                }

                button(fomantic.ui.tertiary.button).text("Abbrechen").on.click {
                    modal.close()
                }
            }
        }
    }
}