package de.doctag.docsrv.ui.admin

import de.doctag.docsrv.model.host
import de.doctag.docsrv.ui.centeredBox
import kweb.ElementCreator
import kweb.WebBrowser
import kweb.h2
import kweb.state.KVar
import kweb.state.render
import java.io.File
import kotlin.random.Random


enum class SetupSteps {
    PIN_CHECK,
    ENTER_DOMAIN_NAME,
    ENTER_PERSONAL_DATA,
    RESULT_PAGE
}

fun WebBrowser.handleInstall(content: ElementCreator<*>) {

    val status = KVar(SetupSteps.PIN_CHECK)

    content.centeredBox {
        h2().text("System einrichten")

        render(status){rStatus->
            when(rStatus){
                SetupSteps.PIN_CHECK -> {
                    val serverPin = Random.nextInt(1_000_000, 9_999_999).toString()

                    val path = System.getProperty("user.home") + File.separator + "installPin_${host()}.txt"
                    val temp: File = File(path)
                    temp.writeText(serverPin + System.lineSeparator())

                    setupPinCheck(serverPin, path){
                        status.value = SetupSteps.ENTER_PERSONAL_DATA
                    }
                }
                SetupSteps.ENTER_DOMAIN_NAME -> {

                }
                SetupSteps.ENTER_PERSONAL_DATA -> {

                }
                SetupSteps.RESULT_PAGE -> {

                }
            }
        }
    }
}
