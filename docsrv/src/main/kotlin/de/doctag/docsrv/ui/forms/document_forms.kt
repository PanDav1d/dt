package de.doctag.docsrv.ui.forms

import com.github.salomonbrys.kotson.fromJson
import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.forms.system.addTagDropdown
import de.doctag.lib.generateRandomString
import de.doctag.lib.toSha1HexString
import io.ktor.util.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import kweb.util.gson
import org.litote.kmongo.findOneById
import java.lang.Exception
import kotlin.random.Random

enum class DocumentAddState{
    UPLOAD,
    INSERT_DOCTAG,
    SAVE
}


data class ImagePositionOnCanvas(val x: Float, val y: Float)
fun ElementCreator<*>.drawDoctagElement(file: FileData, size:Float=4.29f, onSubmit:(file:FileData, doctag:String)->Unit) {
    val doctag = "https://${db().currentConfig.hostname}/d/${generateRandomString(16)}"
    val doctagImg = getQRCodeImageAsDataUrl(doctag, (60*2.5f/4.29f).toInt(), (60*2.5f/4.29f).toInt(), 1)
    val documentImgBi = renderPdfAsImage(file.base64Content!!)
    val documentImg = documentImgBi.asDataUrlImage()
    val fieldResult = KVar<Map<String, KVar<String>>>(mapOf())

    div(fomantic.ui.two.column.grid).new {
        div(fomantic.ui.column).new {
            val canvas = canvas(420, 594).apply {
                this.setAttributeRaw("style", "border: 1px solid black;)")
            }//.focus()

            element("script").text(
                """
        function displayCanvas() {           
            window.canvas0815 = new Canvas0815({
              'canvasElementId': '${canvas.id}', 
              'backgroundImageSrc': '${documentImg}',
              'qrCodeImageSrc': '${doctagImg}',
              'debug': false});           
        }
    """.trimIndent()
            )

            element("script", mapOf("src" to "/ressources/canvas0815.js", "onload" to "displayCanvas()"))
        }
        div(fomantic.ui.column).new {
            renderDocumentInputFields(file)?.let {
                fieldResult.value = it
            }
        }
    }

    div(fomantic.divider.hidden)
    buttonWithLoader(i18n("ui.forms.documentForms.drawDoctagForm.confirm","Übernehmen")){
        val callbackId = Random.nextInt()
        browser.executeWithCallback("callbackWs($callbackId,{x: 1.0*window.canvas0815.qrCodePosX, y: 1.0*window.canvas0815.qrCodePosY});", callbackId){inputData->
            val pos : ImagePositionOnCanvas = gson.fromJson(inputData.toString())
            logger.info("QR Code shall be placed at position ${pos.x}/${pos.y}")


            logger.info("Received form data: ${fieldResult.value}")

            file.base64Content = insertDoctagIntoPDF(file.base64Content!!, doctag, pos.x, pos.y, size, fieldResult.value.map { it.key to it.value.value }.toMap())
            onSubmit(file, doctag)
        }
    }
}


fun ElementCreator<*>.renderDocumentInputFields(fileObj: FileData) : Map<String, KVar<String>>? {

    return fileObj.base64Content?.let {
        val fields = extractFormFieldsFromPdf(it)

        val inputData = mutableMapOf<String, KVar<String>>()

        formControl { form ->
            fields.forEach { field ->

                logger.info("Field is of type ${field.fieldType}")
                val boundKv = inputData.getOrPut(field.fullyQualifiedName, {KVar("")})

                formInput( field.fullyQualifiedName, "", false,  boundKv, InputType.text)
                    .with(form)
            }

        }

        logger.info("Form data is $inputData")

        inputData
    }
}

fun ElementCreator<*>.documentAddForm(documentObj: Document, onSaveClick: (file: FileData, doc: Document)->Unit){
    val document = KVar(documentObj)
    val fileObj = FileData()
    val state = KVar(DocumentAddState.UPLOAD)

    formControl {
        render(state){rState->
            when(rState){
                DocumentAddState.UPLOAD-> {
                    val formField = fileInput(i18n("ui.forms.documentForms.documentAddForm.file","Datei"), "", false, KVar(""), accept = "application/pdf")

                    buttonWithAsyncLoader(i18n("ui.forms.documentForms.documentAddForm.upload","Hochladen")){whenDone->
                        formField.retrieveFile { file ->
                            logger.info("Received file ${file.fileName}")

                            val (contentType, data) = file.base64Content.fromDataUrl()
                            val docId = try {
                                extractDocumentIds(data)?.firstOrNull()?.documentId
                            } catch(ex:Exception){
                                logger.error(ex)
                                logger.error("Failed to extract document id. Assume no document id is present")
                                null
                            }

                            logger.info("Has doctag? ${docId != null}. Full url ${docId?.fullUrl}")

                            fileObj.name = file.fileName
                            fileObj.contentType = contentType
                            fileObj.base64Content = data
                            fileObj._id = data.toSha1HexString()

                            document.value.let {
                                it.url = docId?.fullUrl
                                it.fullText = extractTextFromPdf(data)
                            }

                            state.value = if(docId!=null)DocumentAddState.SAVE else DocumentAddState.INSERT_DOCTAG
                            whenDone()
                        }
                    }
                }
                DocumentAddState.INSERT_DOCTAG -> {

                    h4(fomantic.ui.header).i18nText("ui.forms.documentForms.documentAddForm.insertDoctagButton","Doctag einfügen")

                    val sizeOptions = mapOf<String, String>("Groß" to "4.29", "Mittel" to "2.5", "Klein" to "1.8")
                    val sizeSelection = KVar(sizeOptions.values.first())
                    p().new {
                        span().i18nText("ui.forms.documentForms.documentAddForm.placeDoctagMessage","Positionieren Sie das DocTag mit der Maus an der gewünschten Position")
                        //radioInput("Größe",options = sizeOptions, false, true, sizeSelection)
                    }


                    render(sizeSelection){
                        drawDoctagElement(fileObj, size = sizeSelection.value.toFloatOrNull() ?: 4.29f) { fileWithDoctag, doctag->
                            document.value.url = doctag
                            state.value = DocumentAddState.SAVE
                        }
                    }

                }
                DocumentAddState.SAVE -> {
                    div(fomantic.ui.icon.message).new {
                        i(fomantic.icon.qrcode)
                        div(fomantic.content).new {
                            div(fomantic.header).i18nText("ui.forms.documentForms.documentAddForm.foundDoctag","DocTag erkannt")
                            p().i18nText("ui.forms.documentForms.documentAddForm.foundDoctagDescription","Das hochgeladene Dokument hat das DocTag ${document.value.url}. Drücken Sie auf Speichern um den Import abzuschließen.")
                        }
                    }

                    div(fomantic.ui.field).new {
                        label().i18nText("ui.forms.documentForms.documentAddForm.selectWorkflowLabel","Workflow wählen")
                        val initial = db().currentConfig.workflow?.defaultWorkflowId

                        initial?.let {
                            document.value.workflow = db().workflows.findOneById(initial)
                        }

                        dropdown(db().workflows.find().map { it._id to (it.name ?:"") }.toMap(), KVar(initial)).onSelect { selectedWorkflowId->
                            if(selectedWorkflowId != null) {
                                document.value.workflow = db().workflows.findOneById(selectedWorkflowId)

                                logger.info("Workflow ${document.value.workflow?.name} selected")
                            }
                        }
                    }

                    var tags = KVar(document.value.fullText.determineMatchingTags(db().tags.find().toList()))
                    div(fomantic.ui.field).new {
                        label().i18nText("ui.forms.documentForms.documentAddForm.selectTagsLabel","Tags wählen")
                        render(tags){ value->
                            value.forEach {
                                tag(it, true){ tag->
                                    tags.value = tags.value.filter { it._id != tag._id}
                                }
                            }
                        }
                        addTagDropdown(tags.value){
                            tags.value = tags.value.plus(it.asAttachedTag())
                        }
                    }

                    buttonWithLoader(i18n("ui.forms.documentForms.documentAddForm.saveButton","Speichern")){
                        val doc = document.value
                        doc.isMirrored = DocumentId.parse(doc.url!!).hostname != db().currentConfig.hostname
                        doc.tags = if(!tags.value.isEmpty()) tags.value else null
                        fileObj._id = fileObj.base64Content!!.toSha1HexString()
                        onSaveClick(fileObj, doc)
                    }
                }
            }
        }
    }
}
