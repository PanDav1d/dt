package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.WorkflowAction
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

fun ElementCreator<*>.workflowForm(wf: Workflow, onSaveClick: (wf:Workflow)->Unit) {
    val workflow = KVar(wf)
    val activeActionIdx = KVar(0)


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

        div(fomantic.ui.fluid.search.dropdown).new {
            i(fomantic.icon.dropdown)
            div(fomantic.text).text("Rollen angeben")
        }


        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            onSaveClick(workflow.value)
        }
    }
}