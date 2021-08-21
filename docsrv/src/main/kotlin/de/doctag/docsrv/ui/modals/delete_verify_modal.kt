package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.ui.buttonWithAsyncLoader
import de.doctag.docsrv.ui.modal
import de.doctag.docsrv.ui.tertiary
import kweb.ElementCreator
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import kweb.*

fun ElementCreator<*>.deleteVerifyModal(objectKind: String, elementToDelete: String, onVerifyDelete: ()->Unit) = modal("Löschen bestätigen"){ modal->
    val hasError = KVar<String?>(null)

    render(hasError){ errorMessage->
        errorMessage?.let{
            div(fomantic.ui.message).new {
                div(fomantic.ui.header).text("Fehler beim Löschen")
                p().text(errorMessage!!)
            }
        }
    }

    h3().text("Bitte bestätigen Sie dass Sie den ${objectKind} '$elementToDelete' wirklich löschen möchten")

    buttonWithAsyncLoader("Löschen", fomantic.ui.red.button, renderInline = true){
        try {
            onVerifyDelete()
            modal.close()
        }
        catch(ex:Exception){
            hasError.value=ex.message
        }
    }
    button(fomantic.ui.tertiary.button).text("Abbrechen").on.click {
        modal.close()
    }
}