package de.doctag.docsrv.ui.forms

import com.github.salomonbrys.kotson.fromJson
import de.doctag.docsrv.*
import de.doctag.docsrv.model.*
import de.doctag.docsrv.ui.*
import de.doctag.lib.generateRandomString
import de.doctag.lib.toSha1HexString
import io.ktor.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.jqueryCore.executeOnSelf
import kweb.state.KVar
import kweb.state.property
import kweb.state.render
import kweb.util.gson
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.regex
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.time.ZonedDateTime
import javax.imageio.ImageIO
import kotlin.random.Random

enum class DocumentAddState{
    UPLOAD,
    INSERT_DOCTAG,
    SAVE
}


data class ImagePositionOnCanvas(val x: Float, val y: Float)
fun ElementCreator<*>.drawDoctagElement(file: FileData, onSubmit:(file:FileData, doctag:String)->Unit) {
    val doctag = "https://${db().currentConfig.hostname}/d/${generateRandomString(16)}"
    val doctagImg = getQRCodeImageAsDataUrl(doctag, 60, 60, 1)
    val documentImg = renderPdfAsImage(file.base64Content!!).asDataUrlImage()


    val canvas = canvas(420, 594).apply {
        this.setAttributeRaw("style", "border: 1px solid black;)")
    }//.focus()

    element("script").text("""
        function displayCanvas() {
            canvas = document.getElementById("${canvas.id}");
            context = canvas.getContext("2d");
            
            
            currentX = canvas.width/2;
            currentY = canvas.height/2;
            
            star_img.onload = function() {
                _Go();
            };
            
            background_img.onload = function() {
                context.drawImage(background_img, 0, 0);
            }
            
            background_img.src='${documentImg}';
            star_img.src='${doctagImg}';
            
            canvas.focus();
        }
    """.trimIndent())

    element("script", mapOf("src" to "/ressources/canvas.js", "onload" to "displayCanvas()"))


    div(fomantic.divider.hidden)
    buttonWithLoader("Übernehmen"){
        val callbackId = Random.nextInt()
        browser.executeWithCallback("callbackWs($callbackId,{x: 1.0*currentX/canvas.width, y: 1.0*currentY/canvas.height});", callbackId){inputData->
            val pos : ImagePositionOnCanvas = gson.fromJson(inputData.toString())
            logger.info("QR Code shall be placed at position ${pos.x}/${pos.y}")
            file.base64Content = insertDoctagIntoPDF(file.base64Content!!, doctag, pos.x, pos.y, 4.29f)
            onSubmit(file, doctag)
        }
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
                    val formField = fileInput("Datei", "", false, KVar(""))

                    buttonWithAsyncLoader("Hochladen"){whenDone->
                        formField.retrieveFile { file ->
                            logger.info("Received file ${file.fileName}")

                            val (contentType, data) = file.base64Content.fromDataUrl()
                            val docId = try {
                                extractDocumentIdOrNull(data)
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
                            }

                            state.value = if(docId!=null)DocumentAddState.SAVE else DocumentAddState.INSERT_DOCTAG
                            whenDone()
                        }
                    }
                }
                DocumentAddState.INSERT_DOCTAG -> {

                    h4(fomantic.ui.header).text("Doctag einfügen")
                    p().text("Positionieren Sie das DocTag mit der Maus an der gewünschten Position")
                    drawDoctagElement(fileObj) {fileWithDoctag, doctag->
                        document.value.url = doctag
                        state.value = DocumentAddState.SAVE
                    }
                }
                DocumentAddState.SAVE -> {
                    div(fomantic.ui.icon.message).new {
                        i(fomantic.icon.qrcode)
                        div(fomantic.content).new {
                            div(fomantic.header).text("DocTag erkannt")
                            p().text("Das hochgeladene Dokument hat das DocTag ${document.value.url}. Drücken Sie auf Speichern um den Import abzuschließen.")
                        }
                    }

                    div(fomantic.ui.field).new {
                        label().text("Workflow wählen")
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

                    buttonWithLoader("Speichern"){
                        val doc = document.value
                        doc.isMirrored = DocumentId.parse(doc.url!!).hostname != db().currentConfig.hostname
                        fileObj._id = fileObj.base64Content!!.toSha1HexString()
                        onSaveClick(fileObj, doc)
                    }
                }
            }
        }
    }
}
