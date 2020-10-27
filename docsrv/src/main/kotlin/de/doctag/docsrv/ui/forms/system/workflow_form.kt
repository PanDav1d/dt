package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.WorkflowAction
import de.doctag.docsrv.model.WorkflowInput
import de.doctag.docsrv.model.WorkflowInputKind
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.FomanticUIClasses
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render


fun ElementCreator<*>.workflowForm(wf: Workflow, onSaveClick: (wf:Workflow)->Unit) {
    val workflow = KVar(wf)
    val activeActionIdx = KVar(0)
    val editMode = KVar(false)


    formControl { formCtrl ->
        formInput( "Name", "Name", true, workflow.propertyOrDefault(Workflow::name, ""))
                .with(formCtrl)
                .validate {
                    when{
                        it.isNullOrBlank() -> "Bitte geben Sie einen Namen für den Workflow an"
                        else -> null
                    }
                }


        h4(fomantic.ui.header).text("Rollen")
        render(activeActionIdx){
            render(workflow){ rWorkflow ->
                logger.info("Render workflow")
                div(fomantic.ui.tabular.menu).new{
                    rWorkflow.actions?.forEachIndexed { idx, wfAction ->
                        a(fomantic.item.active(idx == activeActionIdx.value)).on.click {
                            activeActionIdx.value = idx
                        }.new {
                            render(editMode){
                                when{
                                    !editMode.value || idx != activeActionIdx.value -> {
                                        span().text(wfAction.role ?: "unbenannt")

                                        if(idx == activeActionIdx.value) {
                                            span().text(" ")
                                            i(fomantic.icon.edit).on.click {
                                                editMode.value = true
                                            }
                                        }
                                    }
                                    editMode.value && idx == activeActionIdx.value -> {
                                        val nameInput = KVar(wfAction.role ?: "unbenannt")
                                        div(fomantic.ui.icon.input.mini.transparent).apply {
                                            this.setAttributeRaw("style", "margin: -5px;")
                                        }.new {
                                            input(InputType.text, attributes = mapOf("style" to "width: 100px")).apply { value=nameInput }.focus()
                                            button(fomantic.ui.basic.icon.button.mini).on.click {
                                                workflow.value.actions!![activeActionIdx.value].role = nameInput.value
                                                workflow.value = workflow.value
                                                editMode.value = false
                                            }.new {
                                                i(fomantic.icon.check)
                                            }
                                        }
                                    }
                                }

                            }

                        }
                    }
                    div(fomantic.right.menu).new {
                        a(fomantic.item).on.click {
                            workflow.value = rWorkflow.copy(actions = (rWorkflow.actions?:listOf()).plus(WorkflowAction("unbenannt")))
                        }.new {
                            i(fomantic.icon.add)
                            span().text("Hinzufügen")
                        }
                    }
                }

                if(workflow.value.actions?.get(activeActionIdx.value) != null){
                    val editAtIndex = KVar<Int?>(null)
                    render(editAtIndex){selectedIdx->
                        table(fomantic.ui.table.very.basic.celled.table).new {
                            thead().new {
                                tr().new {
                                    th(fomantic.four.wide).text("Feldname")
                                    th(fomantic.four.wide).text("Eingabe Typ")
                                    th(fomantic.six.wide).text("Beschreibung")
                                    th(fomantic.two.wide).text("")
                                }
                            }
                            tbody().new {
                                workflow.value.actions?.get(activeActionIdx.value)?.inputs?.forEachIndexed { index, field ->
                                    if(selectedIdx != index) {
                                        tr().new {
                                            td().text(field.name ?: "")
                                            td().text(field.kind?.toString() ?: "")
                                            td().text(field.description ?: "")
                                            td().new{
                                                i(fomantic.icon.edit).on.click {
                                                    editAtIndex.value = index
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        workflowInputInlineEditForm(field, fomantic.icon.check){newWorkflowInput->
                                            val newVal = rWorkflow.modifyWorkflowActionWithIndex(activeActionIdx.value){ oldAction->
                                                val newInputs = oldAction.inputs?.mapIndexed { index, currentWorkflowInput ->
                                                    if(index == editAtIndex.value) newWorkflowInput else currentWorkflowInput
                                                }
                                                oldAction.copy(inputs = newInputs)
                                            }
                                            workflow.value = newVal
                                            editAtIndex.value = null
                                        }
                                    }
                                }
                            }


                            workflowInputInlineEditForm(WorkflowInput()){ workFlowInput ->
                                val newVal = rWorkflow.copy(actions = workflow.value.actions?.mapIndexed { index, workflowAction ->
                                    if(index == activeActionIdx.value){
                                        workflowAction.copy(inputs = (workflowAction.inputs?:listOf()) + workFlowInput)
                                    }else {
                                        workflowAction
                                    }
                                }?.toList())

                                workflow.value = newVal

                                logger.info("workflowInputInlineEditForm::save finished")
                            }
                        }
                    }
                }

            }
        }

        div(fomantic.ui.divider.hidden)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            onSaveClick(workflow.value)
        }
    }
}

fun ElementCreator<*>.workflowInputInlineEditForm(workFlowInput: WorkflowInput, iconClass :FomanticUIClasses = fomantic.icon.add, saveFunc: (wfi:WorkflowInput)->Unit){
    tr().new {
        val input = KVar(workFlowInput)

        td().new{
            input(InputType.text,placeholder = "Name").apply { value=input.propertyOrDefault(WorkflowInput::name, "") }
        }
        td().new{
            dropdown(mapOf(
                    WorkflowInputKind.Checkbox.name to "Checkbox",
                    WorkflowInputKind.FileInput.name to "Datei anfügen",
                    WorkflowInputKind.TextInput.name to "Texteingabe",
                    WorkflowInputKind.Sign.name to "Signieren"
            )).onSelect{ selectedKey->
                val kind = WorkflowInputKind.valueOf(selectedKey!!)
                input.value.kind = kind
            }
        }
        td().new{
            input(InputType.text, placeholder = "Beschreibung").apply { value=input.propertyOrDefault(WorkflowInput::description, "") }
        }
        td().new{

            a(href = "#").new {
                i(iconClass).on.click {
                    saveFunc(input.value)
                }
            }
        }
    }
}