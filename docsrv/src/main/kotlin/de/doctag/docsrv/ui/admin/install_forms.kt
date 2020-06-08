package de.doctag.docsrv.ui.admin

import de.doctag.docsrv.model.User
import de.doctag.docsrv.model.db
import de.doctag.docsrv.model.host
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import org.litote.kmongo.findOne
import org.litote.kmongo.regex

fun ElementCreator<*>.setupPinCheck(expectedPin: String, fileName: String, whenDone: ()->Unit){

    val actualPin = KVar("")

    formControl { formCtrl ->


        h4(fomantic.ui.header).text("Server-Pin angeben")
        p().innerHTML(
                """Bitte geben Sie den Server-Pin an, um mit der Installation fortzufahren. Die Server-Pin erhalten Sie indem Sie den folgenden Befehl im Terminal Ihres Servers ausführen:""".trimIndent()
        )
        div(fomantic.ui.inverted.segment).new{
            span(fomantic.ui.inverted.purple.text).text("cat ${fileName}")
        }

        formInput("Server-PIN", "bitte angeben", true, actualPin)
                .with(formCtrl)
            .validate { input ->
                if(input != expectedPin){
                    "Bitte geben Sie die korrekte PIN an"
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


        h4(fomantic.ui.header).text("Domain-Name festlegen")
        p().innerHTML(
                """Bitte geben Sie den Domain-Namen ein, unter dem dieses System erreichbar ist.""".trimIndent()
        )

        formInput("Domain-Name", "test.doctag.de", true, domainName)
                .with(formCtrl)
                .validate { input ->
                    if(input == null || !Regex("^([a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,}\$").matches(input)){
                        "Bitte geben Sie einen korrekten Domain-Namen an"
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