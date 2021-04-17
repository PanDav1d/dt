package de.doctag.docsrv.ui.modals.system

import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.forms.system.workflowForm
import de.doctag.docsrv.ui.modal
import kotlinx.coroutines.delay
import kweb.ElementCreator
import org.litote.kmongo.save

fun ElementCreator<*>.addWorkflowModal(onWorkflowAdd: (wf: Workflow)->Unit) = modal("Workflow hinzufügen"){ modal->

    workflowForm(Workflow()) { workflow ->
        db().workflows.save(workflow)

        modal.close()
        onWorkflowAdd(workflow)
    }
}

fun ElementCreator<*>.modifyWorkflowModal(workflow: Workflow, onWorkflowModify: (wf: Workflow)->Unit) = modal("Workflow bearbeiten"){ modal->

    workflowForm(workflow) { workflow ->
        db().workflows.save(workflow)
        modal.close()
        onWorkflowModify(workflow)
    }
}