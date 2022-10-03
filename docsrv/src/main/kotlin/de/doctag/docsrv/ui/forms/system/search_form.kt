package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.extractTextFromPdf
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import java.lang.Exception


fun ElementCreator<*>.search_settings_form(){

    h4(fomantic.ui.header).i18nText("ui.forms.system.searchForm.title","Suche")

    buttonWithLoader(i18n("ui.forms.system.searchForm.reindexButton","Index neu erstellen")){
        db().documents.find().forEach { doc->
            try {
                val file = doc.attachmentId?.let { db().files.findOneById(it) }
                doc.fullText = file?.base64Content?.let { extractTextFromPdf(it) }

                db().documents.save(doc)
            }
            catch(ex:Exception){
                de.doctag.lib.logger.error("Failed to reindex document with id ${doc._id}")
            }
        }
    }

}