package de.doctag.docsrv.ui.admin

import de.doctag.docsrv.model.User
import de.doctag.docsrv.model.db
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


        h2(fomantic.ui.header).text("Server-Pin angeben")
        p().innerHTML(
                """Bitte geben Sie den Server-Pin an, um mit der Installation fortzufahren. Die Server-Pin erhalten Sie indem Sie den folgenden Befehl im Terminal Ihres Servers ausführen:""".trimIndent()
        )
        div(fomantic.ui.inverted.segment).new{
            span(fomantic.ui.inverted.purple.text).text("cat ${fileName}")
        }

        formInput("Server-PIN", "bitte angeben", true, actualPin)
                .with(formCtrl)
                .withInputMissingErrorMessage("Bitte geben Sie den Server-PIN an.")

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            whenDone()
        }
    }
}