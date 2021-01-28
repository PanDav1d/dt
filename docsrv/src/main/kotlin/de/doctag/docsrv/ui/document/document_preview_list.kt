package de.doctag.docsrv.ui.document

import SearchFilter
import de.doctag.docsrv.formatDateTime
import de.doctag.docsrv.isImage
import de.doctag.docsrv.isPdf
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.AttachmentImporter
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.addDocumentModal
import documentSearchFilterComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
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

    return this.documents.find(and(findBson)).sort(descending(Document::created)).toList()

}

fun ElementCreator<*>.handleDocumentPreviewList() {
    authRequired {
        val documents = KVar(db().documents.find().sort(descending(Document::created)).toList())
        val searchQuery = KVar(SearchFilter(""))
        val isImportRunning = KVar(false)
        val documentToPreview = KVar(documents.value.firstOrNull())

        val pageArea = pageHeader("Dokumentenliste")

        searchQuery.addListener{old, new ->
            documents.value = db().handleSearchQueryChange(new)
        }

        div(fomantic.ui.main.container).new {

            div(fomantic.ui.grid).new {

                div(fomantic.row).new {
                    div(fomantic.sixteen.wide.column).new {
                        div(fomantic.ui.vertical.segment).new {

                            h1(fomantic.ui.header).text("Dokumentenliste")
                            div(fomantic.ui.content).new {
                                div(fomantic.content).new() {

                                    render(isImportRunning, container = {div()}){ isRunning->
                                        documentTabMenu(DocumentTabMenuActiveItem.DocumentList) {
                                            val modal = addDocumentModal { doc->
                                                logger.info("Dokument hinzufügen")
                                                pageArea.showToast("Dokument erfolgreich hinzugefügt", ToastKind.Success)
                                                documents.value = listOf(doc).plus(documents.value)
                                            }

                                            a(fomantic.item).text("Dokument hinzufügen").on.click {
                                                modal.open()
                                            }


                                            if(isRunning) {
                                                div(fomantic.ui.active.inline.loader)
                                            }
                                            else {
                                                a(fomantic.item).text("Aus Mail Postfach importieren").on.click {
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
                    div(fomantic.five.wide.column).new {
                        render(documents, container = {div()}){ rDocuments ->
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

                                                documentToPreview.value = document
                                            }
                                        }.new {
                                            td(mapOf("style" to "width:100vw")).new {
                                                div().text(document.originalFileName?.take(35) ?: "")
                                                document.getWorkflowStatus().forEach { (role, signature) ->
                                                    if(signature != null) {
                                                        val signedAt = signature.signed?.formatDateTime()
                                                        i(fomantic.ui.icon.check.circle.outline.green).withPopup(role, "Signiert am ${signedAt} von ${signature.publicKey?.issuer?.name1}")
                                                    }
                                                    else {
                                                        i(fomantic.ui.icon.circle.outline.grey).withPopup(role, "Noch nicht signiert")
                                                    }
                                                }
                                                span(mapOf("style" to "float:right")).text(document.created?.formatDateTime(true) ?: "")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    div(fomantic.eleven.wide.column).new {
                        render(documentToPreview) { rFile ->
                            renderDocumentPreview(rFile)
                        }
                    }
                }
            }
        }
    }
}

fun ElementCreator<*>.renderDocumentPreview(rFile: Document?){

    val file = rFile?.attachmentId?.let{db().files.findOneById(it)}

    div(fomantic.ui.placeholder.segment).also{
        it.setAttributeRaw("style", "height:calc(65px + 70vh);")
    }.new{

        div(fomantic.ui.grid).new {
            div(fomantic.ui.column.twelve.wide).new {
                h1().text(file?.name ?: "Keine Vorschau verfügbar")
            }
            div(fomantic.ui.column.four.wide).new {
                button(fomantic.ui.button.tertiary.blue).apply {
                    this.on.click {
                        val docIdPart = rFile?.url!!.split("/d/")[1]
                        val hostname = rFile?.url!!.split("/d/")[0].removePrefix("https://")
                        if(hostname != db().currentConfig.hostname){
                            browser.navigateTo("/d/${docIdPart}/${hostname}")
                        }
                        else {
                            browser.navigateTo("/d/${docIdPart}")
                        }
                    }
                }.new {
                    i(fomantic.ui.icon.eye)
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
                    element("iframe", mapOf("style" to "height: 100%; width:90%; border: none", "src" to "/d/${rFile._id}/viewSignSheet"))
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

