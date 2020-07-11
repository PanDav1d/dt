package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import de.doctag.lib.model.Address
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar

fun ElementCreator<*>.hostnameEditForm(hostnameStr:String, onSaveClick: (hostname: String)->Unit){
    val hostName = KVar(hostnameStr)

    formControl { formCtrl ->

        h4(fomantic.ui.header).text("Hostname")

        div(fomantic.ui.message.yellow).new {
            div(fomantic.header).text("Achtung")
            p().text("Bitte beachten Sie, dass Ihr System möglicherweise nicht mehr erreichbar sein wird, wenn Sie falsche Angaben in diesem Feld vornehmen.")
        }

        div(fomantic.ui.field).new {
            label().text("Hostname")
            formInput(null, "docsvr.test.de", true, hostName)
                    .with(formCtrl)
                    .withInputMissingErrorMessage("Bitte geben Sie den Domain Namen an, unter dem dieses System erreichbar ist.")
        }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl) {
            onSaveClick(hostName.value)
        }
    }
}