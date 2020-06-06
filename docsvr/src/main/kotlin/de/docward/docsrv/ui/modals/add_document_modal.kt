package de.docward.docsrv.ui.modals

import de.docward.docsrv.model.DbContext
import de.docward.docsrv.model.Document
import de.docward.docsrv.ui.forms.documentAddForm
import de.docward.docsrv.ui.modal
import kweb.ElementCreator
import org.litote.kmongo.save
import java.time.ZonedDateTime

fun ElementCreator<*>.addDocumentModal(onDocumentAdd: (d: Document)->Unit) = modal("Dokument hinzufügen"){ modal->
    val document  = Document()
    documentAddForm(document) { fileObj, docObj ->

        fileObj.apply { DbContext.files.insertOne(fileObj) }

        docObj.attachmentId = fileObj._id
        docObj.created = ZonedDateTime.now()
        docObj.originalFileName = fileObj.name

        docObj.apply {
            DbContext.documents.insertOne(docObj)
        }
        docObj.url = "https://${Config.instance.hostName}/d/${docObj._id}"
        DbContext.documents.save(docObj)

        onDocumentAdd(docObj)

        modal.close()
    }
}