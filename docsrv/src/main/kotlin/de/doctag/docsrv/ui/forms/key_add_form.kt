package de.doctag.docsrv.ui.forms

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
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

        h4(fomantic.ui.dividing.header).i18nText("ui.forms.keyAddForm.displayNameHeader","Anzeigename")
        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.keyAddForm.displayNameLabel","Anzeigename")
            formInput(null, i18n("ui.forms.keyAddForm.displayNamePlaceholder","Vorname"), true, verboseName)
                .with(formCtrl)
                .withInputMissingErrorMessage(i18n("ui.forms.keyAddForm.provideDisplayNameError","Bitte geben Sie einen Anzeigenamen für das Teilnehmerzertifikat an"))
        }

        h4(fomantic.ui.dividing.header).i18nText("ui.forms.keyAddForm.organizationNameHeader","Für Organisation")

        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.keyAddForm.organizationNameLabel","Name")
            formInput(null, i18n("ui.forms.keyAddForm.organizationNamePlaceholder","Name"), true, address.propertyOrDefault(Address::name1, ""))
                .with(formCtrl)
                .withInputMissingErrorMessage(i18n("ui.forms.keyAddForm.provideOrganizationNameErrorMessage","Bitte geben Sie den Namen der zugehörigen Organisation an"))
        }
        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.keyAddForm.name2Label","Name 2")
            formInput(null, i18n("ui.forms.keyAddForm.name2Placeholder","Name 2"), false, address.propertyOrDefault(Address::name2, ""))
                .with(formCtrl)
        }

        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.keyAddForm.streetLabel","Straße")
            formInput(null, i18n("ui.forms.keyAddForm.streetPlaceholder","Straße"), true, address.propertyOrDefault(Address::street, ""))
                .with(formCtrl)
                .withInputMissingErrorMessage(i18n("ui.forms.keyAddForm.pleaseProvideStreetErrorMessage","Bitte geben Sie die Straße der zugehörigen Organisation an"))
        }

        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.keyAddForm.countryCityZipLabel","Land / PLZ / Ort")
            div(fomantic.ui.three.fields).new {
                formInput(null, i18n("ui.forms.keyAddForm.countryPlaceholder","Land"), true, address.propertyOrDefault(Address::countryCode, ""))
                    .with(formCtrl)
                    .validate {
                        when{
                            it?.matches("^[A-Z]{2}$".toRegex()) != true -> i18n("ui.forms.keyAddForm.provideCountryCodeError","Bitte geben Sie ein gültiges Länderkennzeichen an")
                            else -> null
                        }
                    }

                formInput(null, i18n("ui.forms.keyAddForm.zipCodePlaceholder","PLZ"), true, address.propertyOrDefault(Address::zipCode, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage(i18n("ui.forms.keyAddForm.provideZipCodeErrorMessage","Bitte geben Sie die Postleitzahl an"))

                formInput(null, i18n("ui.forms.keyAddForm.cityPlaceholder","Ort"), true, address.propertyOrDefault(Address::city, ""))
                    .with(formCtrl)
                    .withInputMissingErrorMessage(i18n("ui.forms.keyAddForm.provideCityError","Bitte geben Sie den Ort an"))
            }
        }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl) {
            onSaveClick(verboseName.value, address.value)
        }
    }
}
