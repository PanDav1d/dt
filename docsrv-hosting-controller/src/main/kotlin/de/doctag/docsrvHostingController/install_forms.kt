package de.doctag.docsrvHostingController

import de.doctag.docsrv.model.host
import de.doctag.docsrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.eq

fun ElementCreator<*>.setupPinCheck(expectedPin: String, whenDone: ()->Unit){

    val actualPin = KVar("")

    formControl { formCtrl ->


        h4(fomantic.ui.header).text("Server-Pin angeben")
        p().innerHTML(
                """Bitte geben Sie den Server-Pin an, um mit der Installation fortzufahren. Die Server-Pin haben Sie per E-Mail erhalten:""".trimIndent()
        )

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

    val domainName = KVar("")
    val useCustomDomain = KVar("no")

    formControl { formCtrl ->

        render(useCustomDomain) { rUseCustomDomain ->

            h4(fomantic.ui.header).text("Domain-Name festlegen")
            p().innerHTML(
                    """Bitte geben Sie den Domain-Namen ein, unter Ihre Instanz des Docservers erreichbar sein soll .""".trimIndent()
            )

            logger.info("useCustomDomain is ${rUseCustomDomain}")
            if (rUseCustomDomain == "yes") {

                div(fomantic.ui.message).new {
                    div().text("Wenn Sie einen eigenen Domain-Namen verwenden möchten, erstellen Sie bitte folgenden DNS-CNAME Eintrag:")
                    render(domainName) { rDomainName ->
                        span(fomantic.ui.purple.text).text("CNAME ${rDomainName} -> ${Config.instance.baseDomainName}")
                    }
                }
                h2(fomantic.divider.hidden)
            }


            radioInput("Eigener Domain-Name", mapOf("ja" to "yes", "nein" to "no"), required = true, isInline = true, bindTo = useCustomDomain)

            if(rUseCustomDomain != "yes") {
                formInputWithRightLabel("Domain-Name", "test.doctag.de", true, domainName, ".${Config.instance.baseDomainName}")
                        .with(formCtrl)
                        .validate { domainNameValidator(it, true) }
            }
            else {
                formInput("Domain-Name", "test.doctag.de", true, domainName)
                        .with(formCtrl)
                        .validate { domainNameValidator(it, false) }
            }

            displayErrorMessages(formCtrl)

            formSubmitButton(formCtrl) {
                if(rUseCustomDomain == "yes") {
                    whenDone(domainName.value.toLowerCase()+"."+Config.instance.baseDomainName)
                }
                else {
                    whenDone(domainName.value.toLowerCase())
                }
            }
        }
    }
}

fun domainNameValidator( input : String?, onlySubdomain: Boolean) : String? {

    val regex = if(onlySubdomain){
        "^([a-z0-9]+(-[a-z0-9]+)*)\$"
    } else {
        "^([a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,}\$"
    }

    if (input.isNullOrBlank() || !Regex(regex).matches(input)) {
        return "Bitte geben Sie einen korrekten Domain-Namen an"
    }

    if(DbContext.hostedInstances.countDocuments(HostedInstance::domainName eq input?.trim()?.toLowerCase())>0){
        return "Der Domain-Name wurde bereits vergeben. Bitte wählen Sie einen anderen Namen"
    }
    return null
}