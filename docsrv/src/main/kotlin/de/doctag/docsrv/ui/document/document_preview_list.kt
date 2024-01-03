package de.doctag.docsrv.ui.document

import de.doctag.docsrv.i18n
import SearchFilter
import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.AttachmentImporter
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.addDocumentModal
import de.doctag.docsrv.ui.modals.signDocumentModal
import documentSearchFilterComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVal
import kweb.state.KVar
import kweb.state.render
import org.bson.conversions.Bson
import org.litote.kmongo.*


fun <T> debounce(delayInMs: Long = 250, func: (T) -> Unit): (T) -> Unit {
    var lastChange: Long

    return { tVal: T ->
        lastChange = System.currentTimeMillis()

        GlobalScope.launch {
            delay(delayInMs)
            if (System.currentTimeMillis() - lastChange >= 250) {
                func(tVal)
            }
        }
    }
}

fun DbContext.handleSearchQueryChange(sf: SearchFilter) : List<Document>{
    logger.info("Search Term did change. Term: ${sf.searchString}. From ${sf.fromDate} till ${sf.tillDate}")

    val findBson = mutableListOf<Bson>()
    if(sf.searchString.isNotEmpty()) {
        findBson.add(Document::fullText.regex(sf.searchString, "i"))
    }

    if(sf.fromDate != null){
        findBson.add(Document::created gte sf.fromDate)
    }

    if(sf.tillDate != null){
        findBson.add(Document::created lte sf.tillDate)
    }

    if(!sf.tags.isNullOrEmpty()){
        findBson.add(Document::tags/ AttachedTag::_id `in` sf.tags.map { it._id })
    }

    return this.documents.find(and(findBson)).sort(descending(Document::created)).limit(100).toList()

}

fun ElementCreator<*>.handleDocumentPreviewList() {
    authRequired {
        val documents = KVar(db().documents.find().limit(100).sort(descending(Document::created)).toList())
        val searchQuery = KVar(SearchFilter(""))
        val isImportRunning = KVar(false)
        val documentToPreview = KVar(documents.value.firstOrNull())

        val pageArea = pageHeader()

        searchQuery.addListener{ old, new ->
            documents.value = db().handleSearchQueryChange(new)
        }

        div(fomantic.ui.main.container).new {

            div(fomantic.ui.stackable.grid).new {

                div(fomantic.row).new {
                    div(fomantic.sixteen.wide.column).new {
                        div(fomantic.ui.vertical.segment).new {

                            h1(fomantic.ui.header).i18nText("ui.document.documentPreviewList.documentList","Dokumentenliste")
                            div(fomantic.ui.content).new {
                                div(fomantic.content).new() {

                                    render(isImportRunning, container = {div()}){ isRunning->
                                        documentTabMenu(DocumentTabMenuActiveItem.DocumentList) {
                                            val modal = addDocumentModal { doc->
                                                logger.info("Added document!")
                                                pageArea.showToast(i18n("ui.document.documentPreviewList.documentSuccessfullyAddedMessage", "Dokument erfolgreich hinzugefügt"), ToastKind.Success)
                                                documents.value = listOf(doc).plus(documents.value)
                                            }

                                            a(fomantic.item).i18nText("ui.document.documentPreviewList.documentAddButton","Dokument hinzufügen").on.click {
                                                modal.open()
                                            }


                                            if(isRunning) {
                                                div(fomantic.ui.active.inline.loader)
                                            }
                                            else {
                                                a(fomantic.item).i18nText("ui.document.documentPreviewList.importFromMailboxButton","Aus Mail-Postfach importieren").on.click {
                                                    try {
                                                        isImportRunning.value = true
                                                        AttachmentImporter(db()).runImport()
                                                    } finally {
                                                        isImportRunning.value = false
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

                div(fomantic.row).new {
                    div(fomantic.sixteen.wide.column.mobileOnly.tabletOnly).new {
                        render(documents, container = {div()}){ rDocuments ->
                            renderDocumentList(rDocuments, documentToPreview, searchQuery){
                                browser.navigateTo(it)
                            }
                        }
                    }
                    div(fomantic.five.wide.column.computerScreenOnly).new {
                        render(documents, container = {div()}){ rDocuments ->
                            renderDocumentList(rDocuments, documentToPreview, searchQuery){
                                documentToPreview.value = it
                            }
                        }
                    }
                    div(fomantic.eleven.wide.column.computerScreenOnly).new {
                        render(documentToPreview) { rFile ->
                            renderDocumentPreview(rFile)
                        }
                    }
                }
            }
        }
    }
}

fun ElementCreator<*>.renderDocumentList(rDocuments: List<Document>, documentToPreview: KVar<Document?>, searchQuery:KVar<SearchFilter>, onDocumentClick: (d:Document)->Unit){
    logger.info("List of documents did change")
    table(fomantic.ui.selectable.celled.table).new {
        thead().new {
            tr().new {
                th().new{
                    documentSearchFilterComponent(searchQuery.value, debounce {
                        searchQuery.value = it
                    })
                }
            }
        }
        render(documentToPreview, container={tbody(attributes = mapOf("style" to "display: block;height:70vh;overflow-y:scroll"))}) {
            rDocuments.forEach { document ->

                val classN = if(document._id == documentToPreview.value?._id){
                    fomantic.active
                }
                else {
                    mapOf<String, Any>()
                }

                tr(classN).apply {
                    this.on.click {
                        logger.info("Clicked")

                        onDocumentClick(document)
                    }
                }.new {
                    td(mapOf("style" to "width:100vw")).new {
                        div().new{
                            span().text(document.originalFileName?.take(35) ?: "")
                        }
                        div().new {
                            document.getWorkflowStatus().forEach { (role, signature) ->
                                if(signature != null) {
                                    val signedAt = signature.signed?.formatDateTime()
                                    i(fomantic.ui.icon.check.circle.outline.green).withPopup(role, i18n("ui.document.documentPreviewList.signedOnMessage", "Signiert am ${signedAt} von ${signature.signedByKey?.ownerAddress?.name1}"))
                                }
                                else {
                                    i(fomantic.ui.icon.circle.outline.grey).withPopup(role, i18n("ui.document.documentPreviewList.notYetSignedMessage","Noch nicht signiert"))
                                }
                            }
                            span(mapOf("style" to "float:right")).new {
                                document.tags?.forEach {
                                    tag(it, false, size = FomanticUiSize.Mini)
                                }
                                span(mapOf("style" to "width:4px;height:1px;display:inline-block;")).text("")
                                span().text(document.created?.formatDateTime(true) ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun ElementCreator<*>.renderDocumentPreview(rFile: Document?, showLinkToDocumentButton: Boolean=true, showSignButton: Boolean=false , showFileName: Boolean=true){

    val file = rFile?.attachmentId?.let{db().files.findOneById(it)}

    div(/*fomantic.ui.placeholder.segment*/).also{
        it.setAttributeRaw("style", "height:calc(65px + 70vh);")
    }.new{

        if(showFileName||showSignButton) {
            div(fomantic.ui.grid).new {
                div(fomantic.ui.column.twelve.wide).new {
                    if (showFileName) {
                        h1().apply{
                            text(file?.name ?: "Keine Vorschau verfügbar")
                            this.setAttribute("style", KVal("overflow:clip;white-space:nowrap;"))
                        }
                    }
                }
                div(fomantic.ui.column.four.wide.right.aligned).new {
                    if (showLinkToDocumentButton) {
                        button(fomantic.ui.button.tertiary.blue).setAttribute("title", KVal(i18n("ui.document.documentPreviewList.show", "Anzeigen")) ).apply {
                            this.on.click {
                                rFile?.let{
                                    browser.navigateTo(rFile)
                                }
                            }
                        }.new {
                            i(fomantic.ui.icon.folder.open)
                        }
                    }

                    if (rFile != null && showSignButton) {
                        val modal = signDocumentModal(rFile) { signedDocument, _ ->
                            db().documents.save(signedDocument)
                        }
                        if(browser.authenticatedUser != null || rFile.workflow?.actions?.any { it.permissions?.allowAnonymousSubmissions == true } == true) {
                            button(fomantic.ui.button.primary).text("Jetzt Signieren").on.click {
                                modal.open()
                            }
                        } else {
                            span(fomantic.small.text).text("Signaturen ohne Authentifikation sind in diesem Dokument nicht möglich.")
                        }
                    }
                }
            }
        }
        if(file!=null) {
            when {
                file.contentType.isImage() -> {
                    img("/f/${file._id}/download", fomantic.ui.medium.centered.image)
                    div(fomantic.ui.divider.hidden)
                }
                file.contentType.isPdf() -> {
                    //element("iframe", mapOf("style" to "height: 100%; width:90%; border: none", "src" to "/f/${file._id}/view"))
                    element("iframe", mapOf("style" to "height: 100%; width:100%; border: none", "src" to "/web/viewer.html?file=/d/${rFile._id}/viewSignSheet"))
                    div(fomantic.ui.divider.hidden)
                }
                else -> {
                    div(fomantic.ui.icon.header).new {
                        i(fomantic.icon.file.pdf.outline)
                        span().text("Keine Vorschau verfügbar")
                    }
                }
            }
        }
    }
}