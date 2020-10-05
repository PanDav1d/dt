package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.PageArea
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.modals.system.addWorkflowModal
import de.doctag.docsrv.ui.selectable
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

fun ElementCreator<*>.workflowListEditForm(pageArea: PageArea) {
    val workflows = KVar(db().workflows.find().toList())
    
    h4(fomantic.ui.dividing.header).text("Workflows")

    val modal = addWorkflowModal{addedWorkflow ->
        pageArea.showToast("Workflow erfolgreich hinzugefügt", ToastKind.Success)
        workflows.value = db().workflows.find().toList()
    }
    button(fomantic.ui.button.mini).text("Neuer Workflow").on.click {
        modal.open()
    }
    div(fomantic.ui.divider.hidden)

    render(workflows){ rWorkflows->
        table(fomantic.ui.selectable.celled.table).new {
            thead().new {
                tr().new {
                    th().text("Name")
                    th().text("Rollen")
                    th().text("Aktion")
                }
            }
            tbody().new {
                rWorkflows.forEach { wf ->
                    tr().new{
                        td().text(wf.name ?: "")
                        td().text(wf.actions?.map { it.role ?: "" }?.joinToString(",") ?: "")
                        td()
                    }
                }
            }
        }
    }
}