package de.doctag.docsrv.ui.modals.system

import de.doctag.docsrv.model.Tag
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.forms.system.tagForm
import de.doctag.docsrv.ui.modal
import kweb.ElementCreator
import org.litote.kmongo.save

fun ElementCreator<*>.addTagModal(onTagAdd: (tag: Tag)->Unit) = modal("Tag hinzufügen"){ modal->

    tagForm(Tag()) { tag ->
        db().tags.save(tag)
        modal.close()
        onTagAdd(tag)
    }
}

fun ElementCreator<*>.modifyTagModal(tag:Tag, onTagSave: (tag: Tag)->Unit) = modal("Tag bearbeiten"){ modal->

    tagForm(tag) { tag ->
        db().tags.save(tag)
        modal.close()
        onTagSave(tag)
    }
}