package de.doctag.docsrv.ui.forms

import de.doctag.docsrv.extractDocumentIdOrNull
import de.doctag.docsrv.extractQRCode
import de.doctag.docsrv.getImagesFromBase64Content
import de.doctag.docsrv.model.*
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.jqueryCore.executeOnSelf
import kweb.state.KVar
import org.litote.kmongo.findOne
import org.litote.kmongo.regex
import java.time.ZonedDateTime

fun ElementCreator<*>.documentAddForm(documentObj: Document, onSaveClick: (file: FileData, doc: Document)->Unit){
    val document = KVar(documentObj)

    formControl { formCtrl ->

        formCtrl.withValidation {
            null
        }


        formInput("Referenz-Nr", "Dokumenten-Nummer", true, document.propertyOrDefault(Document::externalId, ""))
                .with(formCtrl)
                .withInputMissingErrorMessage("Bitte geben Sie die Dokumentenreferenz an.")

        formInput("Klasse", "Dokumentenklasse", true, document.propertyOrDefault(Document::classifier, ""))
                .with(formCtrl)
                .withInputMissingErrorMessage("Bitte geben Sie die Dokumentenklasse an.")

        var file = KVar("")

        var formField = fileInput("Datei", "", false, file)
                .with(formCtrl)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            formField.retrieveFile { file ->
                logger.info("Received file ${file.fileName}")


                val doc = document.value

                val (contentType, data) = file.base64Content.removePrefix("data:").split(";base64,")

                val docId  = extractDocumentIdOrNull(data)

                if(docId != null){
                    logger.info("Extracted document ID: ${docId.fullUrl}")
                    if(docId.hostname == db().currentConfig.hostname) {
                        doc._id = docId.id
                    }
                    else {
                        doc.isMirrored = true
                    }
                    doc.url = docId.fullUrl
                }
                else {
                    logger.info("No Document ID found")
                }

                val file = FileData(docId?.id, file.fileName, data, contentType)
                onSaveClick(file, doc)
            }

        }
    }
}
