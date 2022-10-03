package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.WorkflowConfig
import de.doctag.docsrv.model.db
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.modals.deleteVerifyModal
import de.doctag.docsrv.ui.modals.system.addWorkflowModal
import de.doctag.docsrv.ui.modals.system.modifyWorkflowModal
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.save

fun ElementCreator<*>.workflowListEditForm(pageArea: PageArea) = useState(1){currentState, setState->
    val rWorkflows = db().workflows.find().toList()

    h4(fomantic.ui.dividing.header).i18nText("ui.forms.system.workflowListEditForm.title", "Workflows")

    val modal = addWorkflowModal{addedWorkflow ->
        pageArea.showToast(i18n("ui.forms.system.workflowListEditForm.successfullyAddedMessage","Workflow erfolgreich hinzugefügt"), ToastKind.Success)
        setState(currentState+1)
    }
    button(fomantic.ui.button.mini).i18nText("ui.forms.system.workflowListEditForm.addWorkflowButton","Neuer Workflow").on.click {
        modal.open()
    }
    div(fomantic.ui.divider.hidden)

    table(fomantic.ui.selectable.celled.table).new {
        thead().new {
            tr().new {
                th().i18nText("ui.forms.system.workflowListEditForm.name","Name")
                th().i18nText("ui.forms.system.workflowListEditForm.roles","Rollen")
                th().i18nText("ui.forms.system.workflowListEditForm.actions","Aktion")
            }
        }
        tbody().new {
            rWorkflows.forEach { wf ->
                tr().new{
                    td().text(wf.name ?: "")
                    td().text(wf.actions?.map { it.role ?: "" }?.joinToString(",") ?: "")
                    td().new {
                        a(href = "#").new {
                            i(fomantic.icon.edit).on.click {
                                modifyWorkflowModal(wf) {
                                    pageArea.showToast(i18n("ui.forms.system.workflowListEditForm.successfullyEdited","Workflow bearbeitet"), ToastKind.Success)
                                    setState(currentState+1)
                                }.open()
                            }.withPopup(null, i18n("ui.forms.system.workflowListEditForm.editWorkflow","Workflow bearbeiten"))
                        }

                        a(href="#").new {
                            if(wf._id == db().currentConfig.workflow?.defaultWorkflowId) {
                                i(fomantic.icon.star).withPopup(null, i18n("ui.forms.system.workflowListEditForm.markAsDefaultWorkflow","Standart-Workflow"))
                            }
                            else {
                                i(fomantic.icon.starOutline).on.click {
                                    db().currentConfig.apply {
                                        if(this.workflow == null) {
                                            this.workflow = WorkflowConfig()
                                        }
                                        this.workflow?.defaultWorkflowId = wf._id
                                        db().config.save(this)
                                    }
                                    pageArea.showToast(i18n("ui.forms.system.workflowListEditForm.successfullySetAsDefaultWorkflow","Standart Workflow festgelegt"), ToastKind.Success)
                                    setState(currentState+1)
                                }.withPopup(null, i18n("ui.forms.system.workflowListEditForm.setAsDefaultWorkflow","Als Standart-Workflow festlegen"))
                            }
                        }
                        a(href="#").new {
                            i(fomantic.icon.remove).on.click {
                                val modal = deleteVerifyModal(i18n("ui.forms.system.workflowListEditForm.deleteModalObjectKind","Workflow"), wf.name ?: ""){
                                    db().workflows.deleteOneById(wf._id!!)
                                }
                                modal.open()
                                modal.onClose {
                                    setState(currentState + 1)
                                }
                            }.withPopup(null, i18n("ui.forms.system.workflowListEditForm.deleteWorkflow","Workflow löschen"))
                        }
                    }
                }
            }
        }
    }
}