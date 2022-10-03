package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
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

        h4(fomantic.ui.header).i18nText("ui.forms.system.designForm.title","Darstellung")


        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.system.designForm.formDescription","Farbe des Seitenkopfs")
            namedColorPicker(conf.property(DesignConfig::headerColor))
        }

        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.system.designForm.titleInputLabel","Titel")
            formInput(null, "DocSrv", true, conf.propertyOrDefault(DesignConfig::headerTitle, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage(i18n("ui.forms.system.designForm.inputMissingErrorMessage","Bitte geben Sie die Titelzeile an, die im Seitenkopf angezeigt werden soll"))
        }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl) {
            onSaveClick(conf.value)
        }
    }
}