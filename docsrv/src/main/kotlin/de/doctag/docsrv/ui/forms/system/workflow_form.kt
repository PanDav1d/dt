package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.WorkflowAction
import de.doctag.docsrv.model.WorkflowInput
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import kweb.*
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
                    table(fomantic.ui.table.very.basic.celled.table).new {
                        thead().new {
                            tr().new {
                                th().text("Feldname")
                                th().text("Eingabe Typ")
                                th().text("Beschreibung")
                                th().text("")
                            }
                        }
                        tbody().new {
                            workflow.value.actions?.get(activeActionIdx.value)?.inputs?.forEach { field->
                                tr().new {
                                    th().text(field.name ?: "")
                                    th().text(field.kind?.toString()?:"")
                                    th().text(field.description?:"")
                                    th().text("")
                                }
                            }

                            tr().new {
                                val input = KVar(WorkflowInput())

                                th().new{
                                    input(InputType.text,placeholder = "Name").apply { value=input.propertyOrDefault(WorkflowInput::name, "") }.focus()
                                }
                                th().new{

                                }
                                th().new{
                                    input(InputType.text, placeholder = "Beschreibung").apply { value=input.propertyOrDefault(WorkflowInput::description, "") }.focus()
                                }
                                th().text("")
                            }
                        }
                    }
                }

/*
                <table class="ui very basic collapsing celled table">
                <thead>
                <tr><th>Employee</th>
                <th>Correct Guesses</th>
                </tr></thead>
                <tbody>
                <tr>
                <td>

 */
            }
        }

        div(fomantic.ui.divider.hidden)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            onSaveClick(workflow.value)
        }
    }
}