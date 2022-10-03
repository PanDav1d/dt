package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.deleteVerifyModal
import de.doctag.docsrv.ui.modals.system.addTagModal
import de.doctag.docsrv.ui.modals.system.modifyTagModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import org.litote.kmongo.deleteOneById


fun ElementCreator<*>.tag_settings_form(pageArea: PageArea) = useState(1){ currentState, setState ->

    val tags = db().tags.find().toList()

    h4(fomantic.ui.dividing.header).i18nText("ui.forms.system.tagListEdit.title","Tags")
    val modal = addTagModal{addedWorkflow ->
        pageArea.showToast(i18n("ui.forms.system.tagListEdit.tagAddedMessage","Tag erfolgreich hinzugefügt"), ToastKind.Success)
        setState(currentState+1)
    }
    button(fomantic.ui.button.mini).i18nText("ui.forms.system.tagListEdit.addTagButton","Neues Tag").on.click {
        modal.open()
    }
    div(fomantic.ui.divider.hidden)


    table(fomantic.ui.selectable.celled.table).new {
        thead().new {
            tr().new {
                th().i18nText("ui.forms.system.tagListEdit.name","Name")
                th().i18nText("ui.forms.system.tagListEdit.desciption","Beschreibung")
                th().i18nText("ui.forms.system.tagListEdit.action","Aktion")
            }
        }
        tbody().new {
            tags.forEach { tag ->
                tr().new {
                    td().new{
                        tag(tag)
                    }
                    td().text(tag.description?:"")
                    td().new {
                        a(href = "#").new {
                            i(fomantic.icon.edit).on.click {
                                modifyTagModal(tag) {
                                    pageArea.showToast(i18n("ui.forms.system.tagListEdit.tagEditedToastMessage","Tag bearbeitet"), ToastKind.Success)
                                    setState(currentState+1)
                                }.open()
                            }.withPopup(null, i18n("ui.forms.system.tagListEdit.editTagText","Tag bearbeiten"))
                        }
                        a(href = "#").new {
                            i(fomantic.icon.remove).on.click {
                                val modal = deleteVerifyModal(i18n("ui.forms.system.tagListEdit.objectKindForDeleteModal","Tag"), tag.name ?: ""){
                                    db().tags.deleteOneById(tag._id!!)
                                }
                                modal.open()
                                modal.onClose {
                                    setState(currentState + 1)
                                }
                            }.withPopup(null, i18n("ui.forms.system.tagListEdit.deleteTagText","Tag löschen"))
                        }
                    }
                }
            }
            if(tags.isEmpty()){
                tr().new {
                    td(mapOf("colspan" to "3")).text("Keine Tags vorhanden")
                }
            }
        }
    }
}