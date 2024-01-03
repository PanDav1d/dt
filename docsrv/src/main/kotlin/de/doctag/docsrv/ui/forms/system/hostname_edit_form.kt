package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import de.doctag.lib.model.Address
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar

fun ElementCreator<*>.hostnameEditForm(hostnameStr:String, onSaveClick: (hostname: String)->Unit){
    val hostName = KVar(hostnameStr)

    formControl { formCtrl ->

        h4(fomantic.ui.header).i18nText("ui.forms.system.hostnameEditForm.title","Hostname")

        div(fomantic.ui.message.yellow).new {
            div(fomantic.header).i18nText("ui.forms.system.hostnameEditForm.attentionBoxTitle","Achtung")
            p().i18nText("ui.forms.system.hostnameEditForm.attentionBoxtext","Bitte beachten Sie, dass Ihr System möglicherweise nicht mehr erreichbar sein wird, wenn Sie falsche Angaben in diesem Feld vornehmen.")
        }

        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.system.hostnameEditForm.hostnameLabel","Hostname")
            formInput(null, "docsvr.test.de", true, hostName)
                    .with(formCtrl)
                    .withInputMissingErrorMessage(i18n("ui.forms.system.hostnameEditForm.hostnameMissingErrorMessage","Bitte geben Sie den Domain-Namen an, unter dem dieses System erreichbar ist."))
        }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl) {
            onSaveClick(hostName.value)
        }
    }
}