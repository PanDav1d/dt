package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.ui.buttonWithAsyncLoader
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.tertiary
import kweb.ElementCreator
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import kweb.*

fun ElementCreator<*>.deleteVerifyModal(objectKind: String, elementToDelete: String, article: String="den", onVerifyDelete: ()->Unit) = modal(i18n("ui.modals.deleteVerifyModal.title","Löschen bestätigen")){ modal->
    val hasError = KVar<String?>(null)

    render(hasError){ errorMessage->
        errorMessage?.let{
            div(fomantic.ui.message).new {
                div(fomantic.ui.header).i18nText("ui.modals.deleteVerifyModal.errorMessage","Fehler beim Löschen")
                p().text(errorMessage!!)
            }
        }
    }

    h3().i18nText("ui.modals.deleteVerifyModal.confirmText","Bitte bestätigen Sie dass Sie ${article} ${objectKind} '$elementToDelete' wirklich löschen möchten")

    buttonWithAsyncLoader(i18n("ui.modals.deleteVerifyModal.deleteButton","Löschen"), fomantic.ui.red.button, renderInline = true){
        try {
            onVerifyDelete()
            modal.close()
        }
        catch(ex:Exception){
            hasError.value=ex.message
        }
    }
    button(fomantic.ui.tertiary.button).i18nText("ui.modals.deleteVerifyModal.abortButton","Abbrechen").on.click {
        modal.close()
    }
}