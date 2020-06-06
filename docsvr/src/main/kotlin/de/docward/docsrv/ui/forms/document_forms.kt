package de.docward.docsrv.ui.forms

import de.docward.docsrv.model.DbContext
import de.docward.docsrv.model.Document
import de.docward.docsrv.model.FileData
import de.docward.docsrv.model.User
import de.docward.docsrv.propertyOrDefault
import de.docward.docsrv.ui.*
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

        var file = KVar<String>("")

        var formField = fileInput("Datei", "", false, file)
                .with(formCtrl)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            formField.retrieveFile { file ->
                logger.info("Received file ${file.fileName}")
                val doc = document.value

                val (contentType, data) = file.base64Content.removePrefix("data:").split(";base64,")

                val file = FileData(null, file.fileName!!, data, contentType)
                onSaveClick(file, doc)
            }

        }
    }
}
