package de.doctag.docsrv.ui.document

import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.getQRCodeImageAsDataUrl
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.document.components.DocumentViewActiveItem
import de.doctag.docsrv.ui.document.components.documentViewTabMenu
import de.doctag.docsrv.ui.forms.system.addTagDropdown
import de.doctag.docsrv.ui.modals.deleteVerifyModal
import de.doctag.docsrv.ui.modals.filePreviewModal
import de.doctag.docsrv.ui.modals.signDocumentModal
import doctag.translation.I18n
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.*


fun ElementCreator<*>.handleDocument(docId: String?, hostname: String?, subPage: String) {

    val document = KVar(db().documents.findOne(Document::url eq "https://${hostname ?: db().currentConfig.hostname}/d/${docId}")!!)
    val selectedSignature = KVar<Int?>(null)

    val activeItem = DocumentViewActiveItem.valueOf(subPage.toUpperCase())



    pageBorderAndTitle(i18n("ui.document.documentView.title", "Dokument"),{conditionalRegisterButton();signButton(document.value)}) { pageArea ->
        div(fomantic.content).new() {
            render(document) { rDocument: Document ->

                logger.info("List of documents did change")


                div(fomantic.content).new() {

                    documentViewTabMenu(docId, hostname, activeItem)


                    when (activeItem) {
                        DocumentViewActiveItem.PREVIEW ->{
                            renderDocumentPreview(rDocument, showLinkToDocumentButton = false, showSignButton = false, showFileName = false)
                        }
                        DocumentViewActiveItem.DETAILS -> {
                            renderDocumentInfo(rDocument, selectedSignature)
                        }
                    }

                }
            }
        }
    }
}

fun ElementCreator<*>.conditionalRegisterButton(){
    if(browser.authenticatedUser==null) {
        button(fomantic.ui.button.tertiary).i18nText("ui.document.documentView.createDoctagInstanceLink","Eigenen Login erhalten").on.click {
            browser.navigateTo("https://www.doctag.de/kostenlos-starten")
        }
    }
}

fun ElementCreator<*>.signButton(rFile: Document){
    val modal = signDocumentModal(rFile) { signedDocument, _ ->
        db().documents.save(signedDocument)
    }

    if(browser.authenticatedUser != null || rFile.workflow?.actions?.any { it.permissions?.allowAnonymousSubmissions == true } == true) {
        if((rFile.getAvailableWorkflowActions(browser.authenticatedUser != null, db().currentConfig.hostname)?.size ?: 0) > 0) {
            button(fomantic.ui.button.primary).i18nText(
                "ui.document.documentView.signNow",
                "Jetzt Signieren"
            ).on.click {
                modal.open()
            }
        } else {
            span(fomantic.small.text).i18nText("ui.document.documentView.signingNoLongerPossible", "Dokument kann nicht mehr signiert werden")
        }
    } else {
        span(fomantic.small.text).i18nText("ui.document.documentView.anonymousSignaturesNotPossibleInfoMessage", "Signaturen ohne Authentifikation sind in diesem Dokument nicht möglich.")
    }
}

fun ElementCreator<*>.renderDocumentInfo(rDocument: Document, selectedSignature: KVar<Int?>) = useState(1) {state, setState ->
    val isSignatureValid = KVar<Boolean?>(null)
    GlobalScope.launch { delay(500); isSignatureValid.value = rDocument.toEmbeddedDocument(db()).validateSignatures() }

    div(fomantic.ui.stackable.two.column.grid).new{
        div(fomantic.column).new {
            h4(fomantic.ui.horizontal.divider.header).i18nText("ui.document.documentView.infosLabel", "Infos")
            div(fomantic.ui.relaxed.list).new {
                div(fomantic.ui.item).new {
                    i(fomantic.ui.paperclip.icon)
                    div(fomantic.ui.content).new {
                        span(fomantic.header).text(rDocument.originalFileName ?: "")
                        div(fomantic.description).i18nText("ui.document.documentView.fileName", "Dateiname")
                    }
                }
                div(fomantic.ui.item).new {
                    i(fomantic.ui.calendarDay.icon)
                    div(fomantic.ui.content).new {
                        span(fomantic.header).text(rDocument.created?.formatDateTime() ?: "")
                        div(fomantic.description).i18nText("ui.document.documentView.creationDate", "Erstellungsdatum")
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
                        div(fomantic.description).i18nText("ui.document.documentView.documentUrl","Dokumentenaddresse")
                    }
                }

                div(fomantic.ui.item).new {
                    i(fomantic.ui.eye.outline.icon)
                    div(fomantic.ui.content).new {
                        span(fomantic.header).new {

                            val userCanEditTags = this.browser.authenticatedUser != null

                            rDocument.tags?.forEach {
                                tag(it, showRemoveButton = userCanEditTags, size = FomanticUiSize.Mini){
                                    rDocument.tags = rDocument.tags?.filter { t->t._id != it._id }
                                    db().documents.save(rDocument)
                                    setState(state+1)
                                }
                            }
                            if(rDocument.tags.isNullOrEmpty()){
                                span().i18nText("ui.document.documentView.noneText","keine")
                            }
                            if(userCanEditTags) {
                                addTagDropdown(rDocument.tags) {
                                    rDocument.tags = (rDocument.tags ?: listOf()).plus(it.asAttachedTag())
                                    db().documents.save(rDocument)
                                    setState(state + 1)
                                    logger.info("Added tag ${it.name}")
                                }
                            }
                        }
                        div(fomantic.description).new{
                            span().i18nText("ui.document.documentView.tags","Tags")
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
                                true -> span().i18nText("ui.document.documentView.signatureValid","Signaturen gültig ")
                                false -> span().i18nText("ui.document.documentView.signatureNotValid","Signaturen ungültig ")
                                null -> span().i18nText("ui.document.documentView.validationRunning","Validierung läuft ")
                            }

                        }
                        div(fomantic.description).new{
                            div(fomantic.description).i18nText("ui.document.documentView.status","Status")
                        }
                    }
                }
            }
        }

        div(fomantic.three.wide.column.right.floated).new {
            h4(fomantic.ui.horizontal.divider.header).i18nText("ui.document.documentView.actions","Aktionen")

            rDocument.url?.let {url->

                val m = modal(i18n("ui.document.documentView.documentAddress","Dokumentenaddresse")){
                    img(src= getQRCodeImageAsDataUrl(url, 400,400, 5))
                    a(href=url).text(url)
                }

                div(fomantic.ui.item).new {
                    button(fomantic.ui.button.tertiary.blue).i18nText("ui.document.documentView.viewDoctagButton","Doctag anzeigen").on.click {
                        m.open()
                    }
                }
            }

            if(this.browser.authenticatedUser != null || rDocument.workflow?.actions?.any { it.permissions?.allowAnonymousSubmissions == true } == true) {
                div(fomantic.ui.item).new {
                    val modal = signDocumentModal(rDocument){signedDocument,_->
                        db().documents.save(signedDocument)
                    }
                    button(fomantic.ui.button.tertiary.blue).i18nText("ui.document.documentView.signButton","Signieren").on.click {
                        modal.open()
                    }
                }
            }

            div(fomantic.ui.item).new {
                a(href = "/d/${rDocument._id}/download", attributes = mapOf("download" to "", "class" to "ui button tertiary blue")).i18nText("ui.document.documentView.downloadButton","Herunterladen")
            }

            if(this.browser.authenticatedUser != null) {
                div(fomantic.ui.item).new {
                    val modal = deleteVerifyModal(I18n.t("ui.document.documentView.verifyDeleteModal.document", "Dokument"), rDocument.originalFileName ?: "unbekannt", I18n.t("ui.document.documentView.verifyDeleteModal.the","das")) {

                        val attachedFiles =
                            rDocument.signatures?.flatMap { it.inputs?.mapNotNull { it.fileId } ?: listOf() }

                        db().files.deleteOneById(rDocument.attachmentId!!)
                        attachedFiles?.forEach { f ->
                            db().files.deleteOneById(f)
                        }
                        db().documents.deleteOneById(rDocument._id!!)
                        browser.navigateTo("/documents")
                    }
                    button(fomantic.ui.button.tertiary.red).i18nText(
                        "ui.document.documentView.deleteButton",
                        "Löschen"
                    ).on.click {
                        modal.open()
                    }
                }
            }
        }
    }



    h2(fomantic.ui.header).new {
        span().i18nText("ui.document.documentView.signatures","Signaturen")
    }

    if((rDocument.signatures?.count() ?: 0) > 0 || rDocument.workflow != null) {
        render(selectedSignature){selectedSignatureIdx ->
            table(fomantic.ui.very.basic.selectable.table.compact).new {
                thead().new {
                    tr().new {
                        th().i18nText("ui.document.documentView.role","Rolle")
                        th().i18nText("ui.document.documentView.operator","Systembetreiber")
                        th().i18nText("ui.document.documentView.address","Addresse")
                        th().i18nText("ui.document.documentView.keyOwner","Schlüsselinhaber")
                        th().i18nText("ui.document.documentView.timestamp","Datum")
                        th().i18nText("ui.document.documentView.info","Info")
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
                            td().text(sig.role ?: i18n("ui.document.documentView.notAvailableText","n.v."))
                            td().text(sig.signedByKey?.ownerAddress?.name1 ?: "")
                            td().text("${sig.signedByKey?.ownerAddress?.zipCode ?: ""} ${sig.signedByKey?.ownerAddress?.city ?: ""}")
                            td().text("${sig.signedByKey?.owner?.firstName} ${sig.signedByKey?.owner?.lastName}")
                            td().text(sig.signed?.formatDateTime() ?: "")
                            td().new {
                                i(fomantic.icon.qrcode).withPopup(i18n("ui.document.documentView.signingDoctagInstance","Signierende Doctag-Instanz"), "${sig.signedByKey?.signingDoctagInstance}")
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
                                                    th().i18nText("ui.document.documentView.name","Name")
                                                    th().i18nText("ui.document.documentView.value","Wert")
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
                                        span(fomantic.ui.grey.text).i18nText("ui.document.documentView.noWorkflowsFoundInfoText","Keine Workflow-Eingaben vorhanden")
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
                                span(fomantic.ui.grey.text).i18nText("ui.document.documentView.noSignaturePresentYetInfoText","Noch keine Signatur vorhanden")
                            }
                        }
                    }
                }
            }
        }
    }
    else {
        p().i18nText("ui.document.documentView.noSignaturesAvailable","Keine Signaturen vorhanden")
    }
}
