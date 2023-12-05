package de.doctag.docsrv.ui.auth

import de.doctag.docsrv.checkPasswordHash
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
import de.doctag.docsrv.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.regex
import org.litote.kmongo.findOne

data class LoginState(
        val isLoading: Boolean = false,
        val user: KVar<String> = KVar(""),
        val password: KVar<String> = KVar(""),
        val errorFlag: Boolean = false
)

fun ElementCreator<*>.handleLogin(){

    val sessionId = browser.httpRequestInfo.request.queryParameters["sessionId"]
    if(sessionId != null){
        logger.info("Found SessionID $sessionId in URL")
        val user = db().users.findOne { User::sessions / Session::sessionId eq sessionId }

        if(user != null){
            logger.info("Authenticated as ${user.firstName} ${user.lastName} via URL")
            Sessions.start(this.browser, this.browser.getOrCreateSessionId()!!, user)

            centeredBox {

                h3().apply {
                    i18nText("ui.auth.login.qr.hello","Hallo ${user.firstName} ${user.lastName}")
                }
                p().apply {
                    text.value = this@centeredBox.i18n("ui.auth.login.qr.loginSuccessful","Sie werden in 3 Sekunden zum Dokumentenindex weitergeleitet.")
                }
                a(href = "/login").i18nText("ui.auth.login.qr.forwardToDokumentIndexLinkText","Weiter")
            }
            GlobalScope.launch {
                delay(3000)
                browser.navigateTo("/")
            }
        }
    }
    else {
        showLoginForm()
    }
}

fun ElementCreator<*>.showLoginForm() = useState(LoginState()){ state, setState->
    val doLogin = {
        setState(state.copy(isLoading = true))
        logger.info("Username ${state.user.value} Passwort ${"*".repeat(state.password.value.length)}")

        val user = db().users.findOne(User::emailAdress.regex(state.user.value, "i"))
        if (user != null && checkPasswordHash(user.passwordHash, state.password.value)) {

            Sessions.start(this.browser, this.browser.getOrCreateSessionId()!!, user)
            this.browser.navigateTo("/")
        } else {
            setState(state.copy(isLoading = false, errorFlag = true))
        }
    }

    centeredBox {
        img(src = "./ressources/logo_inverse.svg")

        div(fomantic.divider.hidden)
        h2().i18nText("ui.auth.loginHeader", "Anmeldung erforderlich")


        if (state.errorFlag) {
            div(fomantic.ui.vertical.segment).new {
                div(fomantic.ui.message.error).new {
                    p().innerHTML(
                        i18n(
                            "ui.auth.login.loginFailedErrorMessage", """
                        Anmeldung fehlgeschlagen. Bitte überprüfen Sie Ihren Benutzernamen und Ihr Passwort.
                        """
                        )
                            .trimIndent()
                    )
                }
            }
        }

        form(fomantic.ui.form).new() {

            div(fomantic.ui.field).new {
                div(fomantic.ui.left.icon.input).new() {
                    i(fomantic.ui.user.icon)
                    input(
                        InputType.text,
                        "email",
                        placeholder = i18n("ui.auth.login.usernamePlaceholder", "Benutzername")
                    ).apply { value = state.user }
                }
            }

            div(fomantic.ui.field).new {
                div(fomantic.ui.left.icon.input).new() {
                    i(fomantic.icon.lock)
                    input(
                        InputType.password,
                        "password",
                        placeholder = i18n("ui.auth.passwordPlaceholder", "Passwort")
                    ).apply { value = state.password }.on.keyup {
                        if (it.code == "Enter") {
                            doLogin()
                        }
                    }
                }
            }

            button(fomantic.ui.button.loading(state.isLoading)).i18nText("ui.auth.loginButtonText", "Anmelden")
                .apply {
                    //text.value="Anmelden"
                    on.click {
                        doLogin()
                    }
                }
        }
    }
}