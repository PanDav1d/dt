package de.doctag.docsrv.ui.modals.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.active
import de.doctag.docsrv.ui.document.DocumentTabMenuActiveItem
import de.doctag.docsrv.ui.forms.system.workflowDependencyForm
import de.doctag.docsrv.ui.forms.system.workflowForm
import de.doctag.docsrv.ui.modal
import kweb.ElementCreator
import kweb.a
import kweb.div
import kweb.new
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save

fun ElementCreator<*>.addWorkflowModal(onWorkflowAdd: (wf: Workflow)->Unit) = modal(i18n("ui.modals.system.workflowModal.addWorkflowModal.title","Workflow hinzufügen")){ modal->

    workflowForm(Workflow()) { workflow ->
        db().workflows.save(workflow)
        modal.close()
        onWorkflowAdd(workflow)
    }
}

enum class ModifyWorkflowActiveItem{
    MAIN,
    DEPENDENCIES
}
fun ElementCreator<*>.workflowModifyTabMenu(activeItem: KVar<ModifyWorkflowActiveItem>)  {
    render(activeItem){
        div(fomantic.ui.secondary.pointing.menu).new{
            a(fomantic.ui.item.active(activeItem.value == ModifyWorkflowActiveItem.MAIN), "#").i18nText("ui.modals.system.workflowModal.menu.main", "Allgemein").on.click { activeItem.value=ModifyWorkflowActiveItem.MAIN }
            a(fomantic.ui.item.active(activeItem.value == ModifyWorkflowActiveItem.DEPENDENCIES), "#").i18nText("ui.modals.system.workflowModal.menu.dependencies", "Abhängigkeiten").on.click { activeItem.value=ModifyWorkflowActiveItem.DEPENDENCIES }
        }
    }
}

fun ElementCreator<*>.modifyWorkflowModal(workflow: Workflow, onWorkflowModify: (wf: Workflow)->Unit) = modal(i18n("ui.modals.system.workflowModal.modifyWorkflowModal.title","Workflow bearbeiten")){ modal->

    val activePage = KVar(ModifyWorkflowActiveItem.MAIN)
    workflowModifyTabMenu(activePage)
    div(fomantic.ui.divider.hidden)

    render(activePage) { cPage ->
        when (cPage) {
            ModifyWorkflowActiveItem.MAIN -> {
                workflowForm(workflow) { workflow ->
                    db().workflows.save(workflow)
                    modal.close()
                    onWorkflowModify(workflow)
                }
            }
            ModifyWorkflowActiveItem.DEPENDENCIES -> {
                workflowDependencyForm(workflow) { workflow ->
                    db().workflows.save(workflow)
                    modal.close()
                    onWorkflowModify(workflow)
                }
            }
        }
    }
}

