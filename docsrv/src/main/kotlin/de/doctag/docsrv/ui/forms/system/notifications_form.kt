package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.PageArea
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.modals.system.addTagModal
import de.doctag.docsrv.ui.selectable
import de.doctag.docsrv.ui.useState
import kweb.*
import kweb.plugins.fomanticUI.fomantic

fun ElementCreator<*>.notifications_form(pageArea: PageArea) = useState(1) { currentState, setState ->
    val notifications = db().notificationRules.find().toList()

    h4(fomantic.ui.dividing.header).text("Benachrichtigungen")
    val modal = addTagModal{addedWorkflow ->
        pageArea.showToast("Benachrichtigung erfolgreich hinzugefügt", ToastKind.Success)
        setState(currentState+1)
    }
    button(fomantic.ui.button.mini).text("Neue Benachrichtigung").on.click {
        modal.open()
    }
    div(fomantic.ui.divider.hidden)


    table(fomantic.ui.selectable.celled.table).new {
        thead().new {
            tr().new {
                th().text("Empfänger")
                th().text("Beschreibung")
                th().text("Aktion")
            }
        }
        tbody().new {
            notifications.forEach { rule ->
                tr().new {
                    td().new{
                        rule.receiver?.forEach {  receiver->
                            div().text("${receiver.name} (${receiver.email})")
                        }
                    }
                    td().text(rule.description?:"")
                    td().new {

                    }
                }
            }
            if(notifications.isEmpty()){
                tr().new {
                    td(mapOf("colspan" to "3")).text("Keine Benachrichtigungsregeln vorhanden")
                }
            }
        }
    }
}