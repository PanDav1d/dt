package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.extractTextFromPdf
import de.doctag.docsrv.i18n
import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.forms.documentAddForm
import de.doctag.docsrv.ui.modal
import de.doctag.lib.toSha1HexString
import kweb.ElementCreator
import org.litote.kmongo.save
import java.time.ZonedDateTime

fun ElementCreator<*>.addDocumentModal(onDocumentAdd: (d: Document)->Unit) = modal(i18n("ui.modals.addDocumentModal.modalTitle","Dokument hinzufügen")){ modal->
    val document  = Document()
    documentAddForm(document) { fileObj, docObj ->

        fileObj.apply { db().files.save(fileObj) }

        docObj._id = fileObj._id
        docObj.attachmentId = fileObj._id
        docObj.attachmentHash = fileObj.base64Content?.toSha1HexString()
        docObj.created = ZonedDateTime.now()
        docObj.originalFileName = fileObj.name
        docObj.fullText = fileObj.base64Content?.let { extractTextFromPdf(it) }


        docObj.apply {
            db().documents.save(docObj)
        }

        if(docObj.url == null) {
            docObj.url = "https://${db().currentConfig.hostname}/d/${docObj._id}"
        }

        db().documents.save(docObj)

        onDocumentAdd(docObj)

        modal.close()
    }
}