package de.doctag.docsrv.ui.modals.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.forms.system.workflowForm
import de.doctag.docsrv.ui.modal
import kweb.ElementCreator
import org.litote.kmongo.save

fun ElementCreator<*>.addWorkflowModal(onWorkflowAdd: (wf: Workflow)->Unit) = modal(i18n("ui.modals.system.workflowModal.addWorkflowModal.title","Workflow hinzufügen")){ modal->

    workflowForm(Workflow()) { workflow ->
        db().workflows.save(workflow)
        modal.close()
        onWorkflowAdd(workflow)
    }
}

fun ElementCreator<*>.modifyWorkflowModal(workflow: Workflow, onWorkflowModify: (wf: Workflow)->Unit) = modal(i18n("ui.modals.system.workflowModal.modifyWorkflowModal.title","Workflow bearbeiten")){ modal->

    workflowForm(workflow) { workflow ->
        db().workflows.save(workflow)
        modal.close()
        onWorkflowModify(workflow)
    }
}