package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.DesignConfig
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import de.doctag.lib.model.Address
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.property

fun ElementCreator<*>.designForm(designConfig: DesignConfig, onSaveClick: (designConfig: DesignConfig)->Unit){
    val conf = KVar(designConfig)

    formControl { formCtrl ->

        h4(fomantic.ui.header).text("Darstellung")


        div(fomantic.ui.field).new {
            label().text("Farbe des Seitenkopfs")
            namedColorPicker(conf.property(DesignConfig::headerColor))
        }

        div(fomantic.ui.field).new {
            label().text("Titel")
            formInput(null, "DocSrv", true, conf.propertyOrDefault(DesignConfig::headerTitle, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage("Bitte geben Sie die Titelzeile an, die im Seitenkopf angezeigt werden soll")
        }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl) {
            onSaveClick(conf.value)
        }
    }
}