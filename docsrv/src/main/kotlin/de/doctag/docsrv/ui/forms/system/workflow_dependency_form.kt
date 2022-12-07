package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.Workflow
import de.doctag.docsrv.model.WorkflowActionDependency
import de.doctag.docsrv.ui.*
import doctag.translation.I18n
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

fun ElementCreator<*>.workflowDependencyForm(wf: Workflow, onSaveClick: (wf: Workflow)->Unit) {
    val workflow = KVar(wf)
    val changeCounter = KVar(0)


    render(changeCounter){

        formControl { formCtrl ->
            table(fomantic.ui.selectable.table).new {
                thead().new {
                    tr().new {
                        th().i18nText("ui.forms.system.workflowDependencyForm.header.roleName","Rolle")
                        th().i18nText("ui.forms.system.workflowDependencyForm.header.dependencyKind","Abhängigkeit")
                        th().i18nText("ui.forms.system.workflowDependencyForm.header.ofRole","von Rolle")
                        th().i18nText("ui.forms.system.workflowDependencyForm.header.action","Aktion")
                    }
                }
                tbody().new {
                    if(wf.actions == null || wf.actions!!.all { it.dependencies.isNullOrEmpty() }){
                        tr().new {
                            td(attributes = mapOf("colspan" to 4)).i18nText("ui.forms.system.workflowDependencyForm.noDependencies", "Keine Abhängigkeiten vorhanden")
                        }
                    }

                    wf.actions?.forEach { action->
                        action.dependencies?.forEach { dep->
                            tr().new {
                                td().text(action.role?:"")
                                td().text(
                                    when{
                                        !dep.afterRole.isNullOrEmpty() -> afterText()
                                        !dep.beforeRole.isNullOrEmpty() -> beforeText()
                                        else -> "---"
                                    }
                                )
                                td().text(dep.afterRole ?: dep.beforeRole ?: "---")
                                td().new {
                                    i(fomantic.icon.delete.red).on.click {
                                        workflow.value.actions!!.find { it == action }?.let{
                                            it.dependencies = it.dependencies!!.filter { it != dep}
                                            changeCounter.value += 1
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val roleOptions = wf.actions?.map { it.role to it.role!! }?.toMap() ?: mapOf()
            val kindOptions = mapOf<String?, String>("before" to beforeText(), "after" to afterText())
            val ofRole = KVar<String?>(null)
            val forRole = KVar<String?>(null)
            val ofKind = KVar<String?>(null)
            dropdown(roleOptions, ofRole)
            dropdown(kindOptions, ofKind)
            dropdown(roleOptions, forRole)

            button(fomantic.ui.button).apply {
                text = KVar("+")
                on.click {
                    wf.actions?.find { it.role == ofRole.value }?.let{selectedAction->
                        selectedAction.dependencies = (selectedAction.dependencies ?: listOf()) + WorkflowActionDependency(
                            afterRole = if(ofKind.value == "after") forRole.value else null,
                            beforeRole = if(ofKind.value == "before") forRole.value else null
                        )
                    }
                    ofRole.value = null
                    forRole.value = null
                    ofKind.value = null
                    changeCounter.value += 1
                }
            }

            div(fomantic.ui.divider.hidden)

            displayErrorMessages(formCtrl)

            formSubmitButton(formCtrl){
                onSaveClick(workflow.value)
            }
        }
    }
}

private fun ElementCreator<*>.beforeText() = I18n.t("ui.forms.system.workflowDependencyForm.kind.before", "vor", language = browser.getOrDetectBrowserLanguage())
private fun ElementCreator<*>.afterText() = I18n.t("ui.forms.system.workflowDependencyForm.kind.after", "nach", language = browser.getOrDetectBrowserLanguage())