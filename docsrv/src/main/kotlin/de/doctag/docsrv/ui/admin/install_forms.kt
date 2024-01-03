package de.doctag.docsrv.ui.admin

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.host
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar

fun ElementCreator<*>.setupPinCheck(expectedPin: String, fileName: String, whenDone: ()->Unit){

    val actualPin = KVar("")

    formControl { formCtrl ->


        h4(fomantic.ui.header).i18nText("ui.admin.installForms.serverPin","Server-PIN angeben")
        p().innerHTML(
                i18n("ui.admin.installForms.requestServerPinText","""Bitte geben Sie die Server-PIN an, um mit der Installation fortzufahren. Die Server-PIN erhalten Sie indem Sie den folgenden Befehl im Terminal Ihres Servers ausführen:""").trimIndent()
        )
        div(fomantic.ui.inverted.segment.left.aligned).new{
                span(fomantic.ui.inverted.text).text("Linux")
                br()
                span(fomantic.ui.inverted.purple.text).text("cat $fileName")
        }
        div(fomantic.ui.inverted.segment.left.aligned).new{
            span(fomantic.ui.inverted.text).text("Windows")
            br()
            span(fomantic.ui.inverted.purple.text).text("notepad $fileName")
        }

        formInput(i18n("ui.admin.installForms.serverPinInputLabel","Server-PIN"), i18n("ui.admin.installForms.serverPinPlaceholder","bitte angeben"), true, actualPin)
                .with(formCtrl)
            .validate { input ->
                if(input != expectedPin){
                    i18n("ui.admin.installForms.serverPinIncorrectError","Bitte geben Sie die korrekte PIN an")
                }
                else {
                    null
                }
            }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            whenDone()
        }
    }
}

fun ElementCreator<*>.setupDomainNameForm(whenDone: (domainName: String)->Unit){

    val domainName = KVar(this.browser.host())

    formControl { formCtrl ->


        h4(fomantic.ui.header).i18nText("ui.admin.installForms.setupDomainNameHeadline","Domain-Name festlegen")
        p().innerHTML(
                i18n("ui.admin.installForms.requestDomainNameText","""Bitte geben Sie den Domain-Namen ein, unter dem dieses System erreichbar ist.""").trimIndent()
        )

        formInput(i18n("ui.admin.installForms.domainNameInputLabel", "Domain-Name"), "test.doctag.de", true, domainName)
                .with(formCtrl)
                .validate { input ->
                    if(input == null || !Regex("^([a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,}\$").matches(input)){
                        i18n("ui.admin.installForms.domainNameInvalidError","Bitte geben Sie einen korrekten Domain-Namen an")
                    }
                    else {
                        null
                    }
                }

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            whenDone(domainName.value)
        }
    }
}