package de.docward.docsrv.ui.auth

import de.docward.docsrv.checkPasswordHash
import de.docward.docsrv.generatePasswordHash
import de.docward.docsrv.model.*
import de.docward.docsrv.ui.centeredBox
import de.docward.docsrv.ui.lock
import de.docward.docsrv.ui.navigateTo
import de.docward.docsrv.ui.tertiary
import de.docward.docsrv.urlParameters
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.eq
import org.litote.kmongo.regex
import org.litote.kmongo.findOne
import org.litote.kmongo.replaceOneById
import java.time.ZonedDateTime

fun ElementCreator<*>.handleLogin(){


    centeredBox {

        h2().text("Anmeldung erforderlich")

        val user = KVar<String>("")
        val password = KVar<String>("")
        val errorFlag = KVar(false)

        render(errorFlag){errorVal ->
            if(errorVal){
                div(fomantic.ui.vertical.segment).new {
                    div(fomantic.ui.message.error).new {
                        p().innerHTML(
                            """
                            Anmeldung fehlgeschlagen. Bitte überprüfen Sie Ihren Benutzernamen und Ihr Passwort.
                            """
                                .trimIndent()
                        )
                    }
                }
            }
        }

        form(fomantic.ui.form).new(){

            div(fomantic.ui.field).new {
                div(fomantic.ui.left.icon.input).new() {
                    i(fomantic.ui.user.icon)
                    input(InputType.text, "email", placeholder = "Benutzername").apply { value=user }
                }
            }

            div(fomantic.ui.field).new {
                div(fomantic.ui.left.icon.input).new() {
                    i(fomantic.icon.lock)
                    input(InputType.password, "password", placeholder = "Passwort").apply { value=password }
                }
            }

            button(fomantic.ui.button).text("Anmelden").apply {
                text.value="Anmelden"
                on.click {
                    this.addClasses("loading")
                    logger.info("Username ${user.value} Passwort ${password.value}")

                    val user = DbContext.users.findOne(User::emailAdress.regex(user.value,"i"))
                    if(user != null && checkPasswordHash(user?.passwordHash, password.value)){

                        Sessions.start(this.browser.getOrCreateSessionId()!!, user)
                        //this.browser.url.value = "/?"
                        this.browser.navigateTo("/")
                    }
                    else {
                        errorFlag.value = true
                        this.removeClasses("loading")
                    }
                }
            }

            button(fomantic.ui.button.blue.tertiary).apply {
                text.value="Registrieren"
                on.click {
                    this.browser.url.value = "/register"
                }
            }
        }
    }
}