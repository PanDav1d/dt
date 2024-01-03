package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.InboundMailConfig
import de.doctag.docsrv.model.SecurityConfig
import de.doctag.docsrv.model.db
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.checkBoxInput
import de.doctag.docsrv.ui.dropdown
import de.doctag.docsrv.ui.formControl
import de.doctag.docsrv.ui.formSubmitButton
import de.doctag.lib.model.PrivatePublicKeyPair
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

fun ElementCreator<*>.security_settings_form(saveAction: (SecurityConfig)->Unit){

    h4(fomantic.ui.header).i18nText("ui.forms.system.securitySettingsForm.title","Sicherheit")

    formControl { formCtrl ->
        val conf = db().currentConfig.security ?: SecurityConfig()
        val secConf = KVar(conf)


        render(secConf){
            if(secConf.value.acceptSignaturesByUnverifiedKeys == true) {
                div(fomantic.ui.message.red).new {
                    div(fomantic.header).i18nText("ui.forms.system.securitySettingsForm.attentionTitle","Achtung")
                    p().i18nText("ui.forms.system.securitySettingsForm.attentionText","""
                        Ihr System akzeptiert Signaturen von noch nicht verzifizierten Teilnehmerzertifikaten. 
                        
                        Mit dieser Einstellung ist nicht mehr sichergestellt, dass die im Teilnehmerzertifikat 
                        verwendeten Angaben immer korrekt und vollständig gesetzt wurden. 
                        
                        Sie müssen damit rechnen, dass Ihr System Teilnehmerzertifikate von Partnern empfängt,
                        die falsche Angaben im Teilnehmerzertifikat gemacht haben.
                        
                        Wir empfehlen die Aktivierung dieser Einstellung nur für Testsysteme.
                        """.trimIndent())
                }
            }
        }


        checkBoxInput(
            i18n("ui.forms.system.securitySettingsForm.acceptSignaturesFromUnverifiedKeysCheckbox","Signaturen von unverifizierten Teilnehmerzertifikaten akzeptieren."),
            secConf.propertyOrDefault(SecurityConfig::acceptSignaturesByUnverifiedKeys, false)
        )

        div(fomantic.ui.divider.hidden)

        val keyOptions = db().keys.find().map { it._id to (it.verboseName ?:"")}.toMap()
        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.system.securitySettingsForm.defaultKey","Standart-Schlüssel für anonyme Signaturaktionen")
            dropdown(keyOptions, secConf.propertyOrDefault(SecurityConfig::defaultKeyForAnonymousSubmissions, null)).onSelect { selectedKeyId ->
                logger.info("Selected key: ${selectedKeyId}. key.value = $selectedKeyId" )
            }
        }

        formSubmitButton(formCtrl){
            saveAction(secConf.value)
        }
    }

}