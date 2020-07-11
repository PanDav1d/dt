package de.doctag.docsrv.ui.forms

import de.doctag.docsrv.model.DbContext
import de.doctag.docsrv.model.User
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import de.doctag.lib.model.Address
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.property
import org.litote.kmongo.findOne
import org.litote.kmongo.regex

fun ElementCreator<*>.keyAddForm(onSaveClick: (vname: String, address: Address)->Unit){
    val verboseName = KVar("")
    val address = KVar(Address())

    formControl { formCtrl ->

        h4(fomantic.ui.dividing.header).text("Anzeigename")
        div(fomantic.ui.field).new {
            label().text("Anzeigename")
            formInput(null, "Vorname", true, verboseName)
                .with(formCtrl)
                .withInputMissingErrorMessage("Bitte geben Sie einen Anzeigenamen für den Schlüssel an")
        }

        h4(fomantic.ui.dividing.header).text("Für Organisation")

        div(fomantic.ui.field).new {
            label().text("Name")
            formInput(null, "Name", true, address.propertyOrDefault(Address::name1, ""))
                .with(formCtrl)
                .withInputMissingErrorMessage("Bitte geben Sie den Namen der zugehörigen Organisation an")
        }
        div(fomantic.ui.field).new {
            label().text("Name 2")
            formInput(null, "Name 2", false, address.propertyOrDefault(Address::name2, ""))
                .with(formCtrl)
        }

        div(fomantic.ui.field).new {
            label().text("Straße")
            formInput(null, "Straße", true, address.propertyOrDefault(Address::street, ""))
                .with(formCtrl)
                .withInputMissingErrorMessage("Bitte geben Sie die Straße der zugehörigen Organisation an")
        }

        div(fomantic.ui.field).new {
            label().text("Land / PLZ / Ort")
            div(fomantic.ui.three.fields).new {
                formInput(null, "Land", true, address.propertyOrDefault(Address::countryCode, ""))
                    .with(formCtrl)
                    .validate {
                        when{
                            it?.matches("^[A-Z]{2}$".toRegex()) != true -> "Bitte geben Sie ein gültiges Länderkennzeichen an"
                            else -> null
                        }
                    }

                formInput(null, "PLZ", true, address.propertyOrDefault(Address::zipCode, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage("Bitte geben Sie die Postleitzahl an")

                formInput(null, "Ort", true, address.propertyOrDefault(Address::city, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage("Bitte geben Sie den Ort an")
            }
        }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl) {
            onSaveClick(verboseName.value, address.value)
        }
    }
}
