package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.model.InboundMailConfig
import de.doctag.docsrv.model.SecurityConfig
import de.doctag.docsrv.model.db
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.checkBoxInput
import de.doctag.docsrv.ui.formControl
import de.doctag.docsrv.ui.formSubmitButton
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

fun ElementCreator<*>.security_settings_form(saveAction: (SecurityConfig)->Unit){

    h4(fomantic.ui.header).text("Sicherheit")

    formControl { formCtrl ->
        val conf = db().currentConfig.security ?: SecurityConfig()
        val secConf = KVar(conf)


        render(secConf){
            if(secConf.value.acceptSignaturesByUnverifiedKeys == true) {
                div(fomantic.ui.message.red).new {
                    div(fomantic.header).text("Achtung")
                    p().text("""
                        Ihr System akzeptiert Signaturen von noch nicht verzifizierten Schlüsseln. 
                        
                        Mit dieser Einstellung ist nicht mehr sichergestellt, dass die im Schlüssel 
                        verwendeten Angaben immer korrekt und vollständig gesetzt wurden. 
                        
                        Sie müssen damit rechnen, dass Ihr System Signaturen von Partnern empfängt,
                        die falsche Angaben im Schlüssel gemacht haben.
                        
                        Wir empfehlen die Aktivierung dieser Einstellung nur für Testsysteme.
                        """.trimIndent())
                }
            }
        }


        checkBoxInput(
            "Signaturen von unverifizierten Schlüsseln akzeptieren.",
            secConf.propertyOrDefault(SecurityConfig::acceptSignaturesByUnverifiedKeys, false)
        )

        formSubmitButton(formCtrl){
            saveAction(secConf.value)
        }
    }

}