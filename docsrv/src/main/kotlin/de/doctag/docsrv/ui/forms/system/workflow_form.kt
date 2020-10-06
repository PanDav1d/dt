package de.doctag.docsrv.ui.forms.system

import com.github.salomonbrys.kotson.fromJson
import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.WorkflowAction
import de.doctag.docsrv.model.WorkflowInput
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import kweb.util.gson
import kweb.util.random

/*

<div class="ui selection dropdown">
          <input type="hidden" name="gender">
          <i class="dropdown icon"></i>
          <div class="default text">Gender</div>
          <div class="menu">
              <div class="item" data-value="1">Male</div>
              <div class="item" data-value="0">Female</div>
          </div>
      </div>


* */

class DropdownValueSelectEvent(val selectedValue: String?, val selectedText: String? )

fun ElementCreator<*>.dropdown(options: Map<String, String>) {

    div(fomantic.ui.selection.dropdown).new {
        input(type=InputType.hidden, name="dropdown")
        i(fomantic.icon.dropdown)
        div(fomantic.text.default).text("Auswahl")
        div(fomantic.menu).new{
            options.forEach { (key, displayText) ->
                div(fomantic.item).apply { this.setAttributeRaw("data-value", key) }.text(displayText)
            }
        }
    }

    val callbackId = Math.abs(random.nextInt())
    browser.executeWithCallback("""
        $('.ui.dropdown').dropdown({
            action: 'activate',
            onChange: function(value, text) {
              // custom action
              console.log("changed")
              callbackWs($callbackId,{selectedValue: value, selectedText: text});
            }
        });
        """.trimIndent(), callbackId) {result->
        val selectedData : DropdownValueSelectEvent = gson.fromJson(result.toString())
        logger.info("Dropdown selected ${selectedData?.selectedValue} / ${selectedData?.selectedText}.")
    }
}


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
                                    input(InputType.text,placeholder = "Name").apply { value=input.propertyOrDefault(WorkflowInput::name, "") }
                                }
                                th().new{
                                    dropdown(mapOf("aaa" to "A", "bb" to "B", "ccc" to "C"))
                                }
                                th().new{
                                    input(InputType.text, placeholder = "Beschreibung").apply { value=input.propertyOrDefault(WorkflowInput::description, "") }
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