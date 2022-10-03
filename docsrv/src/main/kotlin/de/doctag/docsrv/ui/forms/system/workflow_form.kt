package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.getQRCodeImageAsDataUrl
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
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

    val unnamed = i18n("ui.forms.system.workflowForm.unnamed","unbenannt")

    formControl { formCtrl ->
        formInput( i18n("ui.forms.system.workflowForm.nameLabel","Name"), i18n("ui.forms.system.workflowForm.namePlaceholder","Name"), true, workflow.propertyOrDefault(Workflow::name, "")).with(formCtrl)
                .validate {
                    when{
                        it.isNullOrBlank() -> i18n("ui.forms.system.workflowForm.nameRequiredErrorMessage","Bitte geben Sie einen Namen für den Workflow an")
                        else -> null
                    }
                }


        h4(fomantic.ui.header).i18nText("ui.forms.system.workflowForm.roles","Rollen")
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
                                        span().text(wfAction.role ?: unnamed)

                                        if(idx == activeActionIdx.value) {
                                            span().text(" ")
                                            i(fomantic.icon.edit).on.click {
                                                editMode.value = true
                                            }
                                        }
                                    }
                                    editMode.value && idx == activeActionIdx.value -> {
                                        val nameInput = KVar(wfAction.role ?: unnamed)
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
                            workflow.value = rWorkflow.copy(actions = (rWorkflow.actions?:listOf()).plus(WorkflowAction(unnamed)))
                        }.new {
                            i(fomantic.icon.add)
                            span().i18nText("ui.forms.system.workflowForm.addActionButton","Hinzufügen")
                        }
                    }
                }

                if(workflow.value.actions?.get(activeActionIdx.value) != null){
                    val editAtIndex = KVar<Int?>(null)
                    render(editAtIndex){selectedIdx->

                        table(fomantic.ui.table.very.basic.table).new {
                            thead().new {
                                tr().new {
                                    th(fomantic.four.wide).i18nText("ui.forms.system.workflowForm.fieldName","Feldname")
                                    th(fomantic.four.wide).i18nText("ui.forms.system.workflowForm.inputKind","Eingabe Typ")
                                    th(fomantic.six.wide).i18nText("ui.forms.system.workflowForm.description","Beschreibung")
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
                                        workflowInputInlineEditForm(field, fomantic.icon.check, showAdvancedOptions = true){newWorkflowInput->
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

                        h4(fomantic.ui.dividing.header).i18nText("ui.forms.system.workflowForm.advancedOptions","Erweiterte Optionen")
                        if(workflow.value.actions?.get(activeActionIdx.value)?.permissions == null){
                            workflow.value.actions?.get(activeActionIdx.value)?.permissions = WorkflowActionPermissions()
                        }

                        val derivedKvar =  KVar(workflow.value.actions?.get(activeActionIdx.value)?.permissions?.allowAnonymousSubmissions ?: false)
                        derivedKvar.addListener{a,b->
                            logger.info("Checkbox Changed from $a to $b")
                            workflow.value.actions?.get(activeActionIdx.value)?.permissions?.allowAnonymousSubmissions = b
                        }

                        div(fomantic.ui.field).new {
                            checkBoxInput(i18n("ui.forms.system.workflowForm.allowAnonymousSignatureCheckbox","Ausfüllen ohne Anmeldung erlauben"),derivedKvar)
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

fun ElementCreator<*>.workflowInputInlineEditForm(workFlowInput: WorkflowInput, iconClass :FomanticUIClasses = fomantic.icon.add, showAdvancedOptions: Boolean=false ,saveFunc: (wfi:WorkflowInput)->Unit){

    val input = KVar(workFlowInput)
    tr().new {

        td().new{
            input(InputType.text,placeholder = i18n("ui.forms.system.workflowForm.fieldNamePlaceholder","Name")).apply { value=input.propertyOrDefault(WorkflowInput::name, "") }
        }
        td().new{
            dropdown(mapOf(
                    WorkflowInputKind.Checkbox.name to i18n("ui.forms.system.workflowForm.inputKindCheckbox","Checkbox"),
                    WorkflowInputKind.FileInput.name to i18n("ui.forms.system.workflowForm.inputKindAttachFile","Datei anfügen"),
                    WorkflowInputKind.TextInput.name to i18n("ui.forms.system.workflowForm.inputKindText","Texteingabe"),
                    WorkflowInputKind.Sign.name to i18n("ui.forms.system.workflowForm.inputKindSignature","Signieren"),
                    WorkflowInputKind.ReceiptMail.name to i18n("ui.forms.system.workflowForm.inputKIndEmail","E-Mailaddresse"),
                ),
            ).onSelect{ selectedKey->
                val kind = WorkflowInputKind.valueOf(selectedKey!!)
                input.value.kind = kind
            }
        }
        td().new{
            input(InputType.text, placeholder = i18n("ui.forms.system.workflowForm.descriptionInputPlaceholder","Beschreibung")).apply { value=input.propertyOrDefault(WorkflowInput::description, "") }
        }
        td().new{

            a(href = "#").new {
                i(iconClass).on.click {
                    saveFunc(input.value)
                }
            }
        }
    }
    if(showAdvancedOptions) {
        render(input.propertyOrDefault(WorkflowInput::kind, WorkflowInputKind.TextInput), container = {tr()}) { inputKind ->
                td(mapOf("colspan" to "3")).new {

                    when (inputKind) {
                        WorkflowInputKind.TextInput -> {
                            div(fomantic.ui.field).new {
                                checkBoxInput(i18n("ui.forms.system.workflowForm.multilineCheckbox","Mehrzeilig?"), KVar(false))
                            }
                        }
                        WorkflowInputKind.Sign -> {
                            val imgChanges = KVar(0)

                            render(imgChanges, {div(fomantic.ui.field)}){
                                if(workFlowInput.options?.signInputOptions?.backgroundImage != null){
                                    label().text(i18n("ui.forms.system.workflowForm.backgroundImage","Hintergrundbild"))

                                    img(src=workFlowInput.options?.signInputOptions?.backgroundImage, mapOf("style" to "max-width: 770px;"))

                                    button(fomantic.ui.button.tertiary).i18nText("ui.forms.system.workflowForm.deleteBackgroundImage","Löschen").on.click {
                                        workFlowInput.options?.signInputOptions?.backgroundImage = null
                                        imgChanges.value += 1
                                    }
                                } else {

                                    val formField = fileInput(
                                        i18n("ui.forms.system.workflowForm.backgroundImageInputLabel","Hintergrundbild"),
                                        "",
                                        false,
                                        KVar(""),
                                        accept = "image/png,image/jepg"
                                    )
                                    formField.onFileSelect {
                                        formField.retrieveFile { upload ->
                                            if (workFlowInput.options == null)
                                                workFlowInput.options = WorkflowInputOptions()

                                            if (workFlowInput.options?.signInputOptions == null)
                                                workFlowInput.options?.signInputOptions = SignInputOptions()

                                            workFlowInput.options?.signInputOptions =
                                                workFlowInput.options?.signInputOptions?.copy(
                                                    backgroundImage = upload.base64Content
                                                )

                                            imgChanges.value += 1
                                        }
                                    }
                                }


                            }
                        }
                        else -> {

                        }
                    }
                }
                td().new {

                }
            }
    }
}